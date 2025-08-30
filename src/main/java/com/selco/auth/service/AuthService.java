package com.selco.auth.service;

import com.selco.auth.dto.LoginRequestDTO;
import com.selco.auth.dto.LoginResponseDTO;
import com.selco.auth.dto.LogoutRequestDTO;
import com.selco.auth.dto.LogoutResponseDTO;
import com.selco.auth.dto.TokenValidationDTO;
import com.selco.auth.events.EmailParaEnvio;
import com.selco.auth.model.LogAcesso;
import com.selco.auth.model.Usuario;
import com.selco.auth.repository.LogAcessoRepository;
import com.selco.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Serviço de autenticação
 * Implementa AUT-13: Endpoint de Login e AUT-14: Lógica de Autenticação
 */
@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LogAcessoRepository logAcessoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private EventPublisherService eventPublisherService;

    /**
     * AUT-13 e AUT-14: Realiza o login do usuário
     */
    public LoginResponseDTO realizarLogin(LoginRequestDTO loginRequest, String userAgent, String ipAddress) {
        try {
            logger.info("Tentativa de login para: {}", loginRequest.getEmail());

            // AUT-14: Verificar se o usuário existe
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(loginRequest.getEmail());
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuário não encontrado: {}", loginRequest.getEmail());
                registrarLogAcesso(null, loginRequest.getEmail(), false, "Usuário não encontrado", userAgent, ipAddress);
                return LoginResponseDTO.erro("Credenciais inválidas");
            }

            Usuario usuario = usuarioOpt.get();

            // AUT-14: Verificar se o usuário está ativo
            if (!"ATIVO".equals(usuario.getStatus())) {
                logger.warn("Usuário inativo tentou fazer login: {}", loginRequest.getEmail());
                registrarLogAcesso(usuario.getId(), loginRequest.getEmail(), false, "Usuário inativo", userAgent, ipAddress);
                return LoginResponseDTO.erro("Usuário inativo. Entre em contato com o administrador.");
            }

            // AUT-14: Verificar senha usando BCrypt
            if (!passwordEncoder.matches(loginRequest.getSenha(), usuario.getSenhaHash())) {
                logger.warn("Senha incorreta para usuário: {}", loginRequest.getEmail());
                registrarLogAcesso(usuario.getId(), loginRequest.getEmail(), false, "Senha incorreta", userAgent, ipAddress);

                // AUT-11: Enviar notificação de tentativa de login com senha incorreta
                try {
                    EmailParaEnvio emailLoginFalha = EmailParaEnvio.notificacaoLogin(
                        usuario.getEmail(),
                        loginRequest.getEmail(),
                        false,
                        ipAddress
                    );
                    eventPublisherService.publicarNotificacaoLogin(emailLoginFalha);
                    logger.debug("Notificação de login falhado publicada para: {}", usuario.getEmail());
                } catch (Exception emailError) {
                    logger.warn("Erro ao enviar notificação de login falhado: {}", emailError.getMessage());
                }

                return LoginResponseDTO.erro("Credenciais inválidas");
            }

            // AUT-15: Gerar tokens JWT
            String accessToken = jwtService.generateAccessToken(
                    usuario.getEmail(),
                    usuario.getTipoUsuario(),
                    usuario.getId()
            );

            String refreshToken = jwtService.generateRefreshToken(usuario.getEmail());

            long expiresIn = jwtService.getAccessTokenExpirationInSeconds();

            // AUT-18: Registrar log de acesso bem-sucedido
            registrarLogAcesso(usuario.getId(), loginRequest.getEmail(), true, "Login realizado com sucesso", userAgent, ipAddress);

            // AUT-11: Enviar notificação de login bem-sucedido por email
            try {
                EmailParaEnvio emailLogin = EmailParaEnvio.notificacaoLogin(
                    usuario.getEmail(),
                    loginRequest.getEmail(), // Usando email como nome por enquanto
                    true,
                    ipAddress
                );
                eventPublisherService.publicarNotificacaoLogin(emailLogin);
                logger.debug("Notificação de login publicada para: {}", usuario.getEmail());
            } catch (Exception emailError) {
                logger.warn("Erro ao enviar notificação de login (login continuará): {}", emailError.getMessage());
            }

            logger.info("Login realizado com sucesso para: {}", loginRequest.getEmail());

            return LoginResponseDTO.sucesso(
                    "Login realizado com sucesso",
                    accessToken,
                    refreshToken,
                    usuario.getTipoUsuario(),
                    expiresIn
            );

        } catch (Exception e) {
            logger.error("Erro durante o login: {}", e.getMessage(), e);
            registrarLogAcesso(null, loginRequest.getEmail(), false, "Erro interno", userAgent, ipAddress);
            return LoginResponseDTO.erro("Erro interno do servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * AUT-18: Registra log de acesso
     */
    private void registrarLogAcesso(String usuarioId, String email, boolean sucesso, String detalhes, String userAgent, String ipAddress) {
        try {
            LogAcesso logAcesso = new LogAcesso();
            logAcesso.setUsuarioId(usuarioId);
            logAcesso.setSucesso(sucesso);
            logAcesso.setMotivo(detalhes);
            logAcesso.setUserAgent(userAgent);
            logAcesso.setIp(ipAddress);
            logAcesso.setDataHora(LocalDateTime.now());

            logAcessoRepository.save(logAcesso);
            
            logger.debug("Log de acesso registrado para: {} - Sucesso: {}", email, sucesso);
        } catch (Exception e) {
            logger.error("Erro ao registrar log de acesso: {}", e.getMessage(), e);
        }
    }

    /**
     * AUT-17: Realiza o logout do usuário
     */
    public LogoutResponseDTO realizarLogout(LogoutRequestDTO logoutRequest, String userAgent, String ipAddress) {
        try {
            String token = logoutRequest.getToken();
            
            logger.info("Tentativa de logout com token");

            // Valida se o token é válido antes de invalidá-lo
            if (token == null || token.trim().isEmpty()) {
                logger.warn("Token vazio fornecido para logout");
                return LogoutResponseDTO.erro("Token inválido");
            }

            // Remove prefixo "Bearer " se presente
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Verifica se o token já está na blacklist
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("Tentativa de logout com token já invalidado");
                return LogoutResponseDTO.erro("Token já foi invalidado");
            }

            // Extrai informações do token para logging
            String email = null;
            try {
                email = jwtService.extractEmail(token);
            } catch (Exception e) {
                logger.warn("Não foi possível extrair email do token para logout: {}", e.getMessage());
            }

            // Adiciona o token à blacklist
            tokenBlacklistService.invalidateToken(token);

            // Registra log de logout
            registrarLogAcesso(null, email, true, "Logout realizado com sucesso", userAgent, ipAddress);

            logger.info("Logout realizado com sucesso para email: {}", email);
            return LogoutResponseDTO.sucesso("Logout realizado com sucesso");

        } catch (Exception e) {
            logger.error("Erro durante o logout: {}", e.getMessage(), e);
            registrarLogAcesso(null, null, false, "Erro no logout: " + e.getMessage(), userAgent, ipAddress);
            return LogoutResponseDTO.erro("Erro interno do servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * AUT-22: Valida um token JWT e retorna informações do usuário
     */
    public TokenValidationDTO validarToken(String token) {
        try {
            logger.info("Validando token JWT");

            // Verifica se o token está na blacklist
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("Token encontrado na blacklist");
                return TokenValidationDTO.invalido("Token foi invalidado");
            }

            // Verifica se o token expirou
            if (jwtService.isTokenExpired(token)) {
                logger.warn("Token JWT expirado");
                return TokenValidationDTO.invalido("Token expirado");
            }

            // Extrai informações do token
            String email = jwtService.extractEmail(token);
            LocalDateTime expiresAt = jwtService.extractExpiration(token).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();

            // Busca o usuário no banco de dados
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
            if (usuarioOpt.isEmpty()) {
                logger.warn("Usuário não encontrado para email: {}", email);
                return TokenValidationDTO.invalido("Usuário não encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Verifica se o token é válido para este usuário
            if (!jwtService.validateToken(token, email)) {
                logger.warn("Token inválido para usuário: {}", email);
                return TokenValidationDTO.invalido("Token inválido para o usuário");
            }

            // Verifica se o usuário está ativo
            if (!"ATIVO".equals(usuario.getStatus())) {
                logger.warn("Usuário não ativo - status: {} para email: {}", usuario.getStatus(), email);
                return TokenValidationDTO.invalido("Usuário não está ativo");
            }

            logger.info("Token válido para usuário: {}", email);
            return TokenValidationDTO.valido(
                    usuario.getId(),
                    email, // Usando email como nome até encontrarmos o campo nome
                    usuario.getEmail(),
                    usuario.getTipoUsuario(),
                    expiresAt
            );

        } catch (Exception e) {
            logger.error("Erro durante validação do token: {}", e.getMessage(), e);
            return TokenValidationDTO.invalido("Erro na validação do token");
        }
    }
}
