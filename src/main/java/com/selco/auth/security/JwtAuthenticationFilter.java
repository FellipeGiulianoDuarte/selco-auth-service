package com.selco.auth.security;

import com.selco.auth.service.JwtService;
import com.selco.auth.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtro JWT para validação de tokens em requisições autenticadas
 * Implementa AUT-20: Filtro de Validação de JWT
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractTokenFromRequest(request);

            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (validateToken(token)) {
                    setAuthenticationContext(token, request);
                } else {
                    logger.debug("Token inválido ou expirado para a requisição: {}", request.getRequestURI());
                }
            }

        } catch (Exception e) {
            logger.error("Erro ao processar token JWT: {}", e.getMessage(), e);
            // Não interrompe a cadeia de filtros, deixa o Spring Security lidar com a ausência de autenticação
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do cabeçalho Authorization
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }
        
        return null;
    }

    /**
     * Valida o token JWT
     */
    private boolean validateToken(String token) {
        try {
            // Verifica se o token está na blacklist
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.debug("Token está na blacklist");
                return false;
            }

            // Verifica se o token não está expirado
            if (jwtService.isTokenExpired(token)) {
                logger.debug("Token expirado");
                return false;
            }

            // Extrai o email para validação adicional
            String email = jwtService.extractEmail(token);
            if (email == null || email.trim().isEmpty()) {
                logger.debug("Token não contém email válido");
                return false;
            }

            // Valida o token com o email
            return jwtService.validateToken(token, email);

        } catch (Exception e) {
            logger.debug("Erro na validação do token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Define o contexto de autenticação do Spring Security
     */
    private void setAuthenticationContext(String token, HttpServletRequest request) {
        try {
            String email = jwtService.extractEmail(token);
            String tipoUsuario = jwtService.extractClaim(token, claims -> claims.get("tipoUsuario", String.class));
            String usuarioId = jwtService.extractClaim(token, claims -> claims.get("usuarioId", String.class));

            // Cria as authorities baseadas no tipo de usuário
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + tipoUsuario));
            
            // Adiciona role genérico de usuário autenticado
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

            // Cria o objeto de autenticação
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(email, null, authorities);
            
            WebAuthenticationDetails webDetails = new WebAuthenticationDetailsSource().buildDetails(request);

            // Define adicional de metadados do usuário se necessário
            authentication.setDetails(new JwtAuthenticationDetails(
                email, usuarioId, tipoUsuario, 
                webDetails
            ));

            // Define no contexto de segurança
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            logger.debug("Autenticação definida para o usuário: {} (Tipo: {})", email, tipoUsuario);

        } catch (Exception e) {
            logger.error("Erro ao definir contexto de autenticação: {}", e.getMessage(), e);
        }
    }

    /**
     * Determina se o filtro deve ser aplicado a esta requisição
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Não aplica o filtro para endpoints públicos
        return path.startsWith("/api/auth/health") ||
               path.startsWith("/api/auth/register") ||
               path.startsWith("/api/auth/login") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator/health");
    }

    /**
     * Classe para armazenar detalhes adicionais da autenticação JWT
     */
    public static class JwtAuthenticationDetails extends WebAuthenticationDetailsSource {
        private final String email;
        private final String usuarioId;
        private final String tipoUsuario;
        private final Object webDetails;

        public JwtAuthenticationDetails(String email, String usuarioId, String tipoUsuario, Object webDetails) {
            this.email = email;
            this.usuarioId = usuarioId;
            this.tipoUsuario = tipoUsuario;
            this.webDetails = webDetails;
        }

        public String getEmail() { return email; }
        public String getUsuarioId() { return usuarioId; }
        public String getTipoUsuario() { return tipoUsuario; }
        public Object getWebDetails() { return webDetails; }
    }
}
