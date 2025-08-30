package com.selco.auth.service;

import com.selco.auth.dto.CadastroFuncionarioDTO;
import com.selco.auth.dto.CadastroResponseDTO;
import com.selco.auth.events.EmailParaEnvio;
import com.selco.auth.events.UsuarioCriado;
import com.selco.auth.model.Usuario;
import com.selco.auth.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

/**
 * Serviço para gerenciar cadastro de funcionários
 * Implementa RF01: Autocadastro de Funcionário
 */
@Service
public class CadastroService {

    private static final Logger logger = LoggerFactory.getLogger(CadastroService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventPublisherService eventPublisherService;

    @Value("${app.email.allowed-domain:@selco.com.br}")
    private String dominioEmpresa;

    private final Random random;

    public CadastroService() {
        this.random = new Random();
    }

    /**
     * Realiza o cadastro de um novo funcionário
     * Conforme RF01 e especificações AUT-06 a AUT-12
     */
    public CadastroResponseDTO cadastrarFuncionario(CadastroFuncionarioDTO dto) {
        try {
            logger.info("Iniciando cadastro de funcionário: {}", dto.getEmail());

            // AUT-09: Validação de domínio de e-mail
            if (!validarDominioEmail(dto.getEmail())) {
                logger.warn("Tentativa de cadastro com domínio de email inválido: {}", dto.getEmail());
                return CadastroResponseDTO.erro("E-mail deve pertencer ao domínio da empresa: " + dominioEmpresa);
            }

            // Verificar se usuário já existe
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(dto.getEmail());
            if (usuarioExistente.isPresent()) {
                logger.warn("Tentativa de cadastro com email já existente: {}", dto.getEmail());
                return CadastroResponseDTO.erro("Já existe um usuário cadastrado com este e-mail");
            }

            // AUT-10: Geração de senha de 6 dígitos
            String senhaTemporaria = gerarSenhaTemporaria();
            String senhaHash = passwordEncoder.encode(senhaTemporaria);

            // AUT-08: Criar modelo Usuario
            Usuario novoUsuario = new Usuario();
            novoUsuario.setEmail(dto.getEmail());
            novoUsuario.setSenhaHash(senhaHash);
            novoUsuario.setTipoUsuario("FUNCIONARIO"); // AUT-12: Valor padrão
            novoUsuario.setStatus("ATIVO"); // AUT-12: Valor padrão
            novoUsuario.setDataCriacao(LocalDateTime.now());
            novoUsuario.setDataAtualizacao(LocalDateTime.now());

            // Salvar no banco
            Usuario usuarioSalvo = usuarioRepository.save(novoUsuario);

            logger.info("Usuário cadastrado com sucesso: {} (ID: {})", dto.getEmail(), usuarioSalvo.getId());

            // AUT-11: Publicar eventos no RabbitMQ
            try {
                // Criar evento UsuarioCriado
                UsuarioCriado usuarioCriadoEvent = new UsuarioCriado(
                    usuarioSalvo.getId(),
                    usuarioSalvo.getEmail(),
                    dto.getNome(),
                    dto.getDepartamento(),
                    dto.getCargo(),
                    usuarioSalvo.getTipoUsuario(),
                    usuarioSalvo.getStatus(),
                    usuarioSalvo.getDataCriacao(),
                    senhaTemporaria
                );

                // Criar evento EmailParaEnvio
                EmailParaEnvio emailEvent = EmailParaEnvio.cadastroFuncionario(
                    dto.getEmail(),
                    dto.getNome(),
                    senhaTemporaria
                );

                // Publicar eventos
                eventPublisherService.publicarEventosCadastro(usuarioCriadoEvent, emailEvent);

                logger.info("Eventos de cadastro publicados com sucesso para: {}", dto.getEmail());

            } catch (Exception eventError) {
                logger.error("Erro ao publicar eventos de cadastro (usuário já foi salvo): {}", 
                           eventError.getMessage(), eventError);
                // Não falha o cadastro por causa dos eventos - usuário já foi criado
                // Em produção, poderia implementar retry ou dead letter queue
            }

            return CadastroResponseDTO.sucesso(
                "Funcionário cadastrado com sucesso. Senha enviada por e-mail.",
                usuarioSalvo.getId()
            );

        } catch (Exception e) {
            logger.error("Erro ao cadastrar funcionário: {}", e.getMessage(), e);
            return CadastroResponseDTO.erro("Erro interno do servidor. Tente novamente mais tarde.");
        }
    }

    /**
     * AUT-09: Valida se o e-mail pertence ao domínio da empresa
     */
    private boolean validarDominioEmail(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String dominio = "@" + email.substring(email.indexOf("@") + 1);
        return dominioEmpresa.equals(dominio);
    }

    /**
     * AUT-10: Gera senha temporária de 6 dígitos numéricos
     */
    private String gerarSenhaTemporaria() {
        return String.format("%06d", random.nextInt(1000000));
    }
}
