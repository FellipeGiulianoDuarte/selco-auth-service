package com.selco.auth.service;

import com.selco.auth.events.UsuarioCriado;
import com.selco.auth.events.EmailParaEnvio;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Serviço para publicação de eventos no RabbitMQ
 * Responsável por enviar eventos para outros microserviços
 */
@Service
public class EventPublisherService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private final ObjectMapper objectMapper;

    // Configurações do RabbitMQ
    @Value("${rabbitmq.exchanges.user:selco.user.exchange}")
    private String userExchange;

    @Value("${rabbitmq.exchanges.email:selco.email.exchange}")
    private String emailExchange;

    @Value("${rabbitmq.routing-keys.user-created:user.created}")
    private String userCreatedRoutingKey;

    @Value("${rabbitmq.routing-keys.email-send:email.send}")
    private String emailSendRoutingKey;

    public EventPublisherService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Publica evento de usuário criado
     * @param evento Evento com dados do usuário criado
     */
    public void publicarUsuarioCriado(UsuarioCriado evento) {
        try {
            logger.info("Publicando evento UsuarioCriado para usuário: {}", evento.getEmail());

            String eventoJson = objectMapper.writeValueAsString(evento);
            
            rabbitTemplate.convertAndSend(
                userExchange,
                userCreatedRoutingKey,
                eventoJson
            );

            logger.info("Evento UsuarioCriado publicado com sucesso para: {}", evento.getEmail());

        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar evento UsuarioCriado: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na serialização do evento UsuarioCriado", e);
        } catch (Exception e) {
            logger.error("Erro ao publicar evento UsuarioCriado: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento UsuarioCriado", e);
        }
    }

    /**
     * Publica evento para envio de email
     * @param evento Evento com dados do email a ser enviado
     */
    public void publicarEmailParaEnvio(EmailParaEnvio evento) {
        try {
            logger.info("Publicando evento EmailParaEnvio para: {} (tipo: {})", 
                       evento.getDestinatario(), evento.getTipoEmail());

            String eventoJson = objectMapper.writeValueAsString(evento);
            
            rabbitTemplate.convertAndSend(
                emailExchange,
                emailSendRoutingKey,
                eventoJson
            );

            logger.info("Evento EmailParaEnvio publicado com sucesso para: {}", 
                       evento.getDestinatario());

        } catch (JsonProcessingException e) {
            logger.error("Erro ao serializar evento EmailParaEnvio: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na serialização do evento EmailParaEnvio", e);
        } catch (Exception e) {
            logger.error("Erro ao publicar evento EmailParaEnvio: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar evento EmailParaEnvio", e);
        }
    }

    /**
     * Publica eventos relacionados ao cadastro de usuário
     * Combina UsuarioCriado + EmailParaEnvio em uma única transação lógica
     */
    public void publicarEventosCadastro(UsuarioCriado usuarioCriado, EmailParaEnvio emailCadastro) {
        try {
            logger.info("Iniciando publicação de eventos de cadastro para: {}", 
                       usuarioCriado.getEmail());

            // Publicar evento de usuário criado
            publicarUsuarioCriado(usuarioCriado);

            // Publicar evento de email de cadastro
            publicarEmailParaEnvio(emailCadastro);

            logger.info("Eventos de cadastro publicados com sucesso para: {}", 
                       usuarioCriado.getEmail());

        } catch (Exception e) {
            logger.error("Erro ao publicar eventos de cadastro: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao publicar eventos de cadastro", e);
        }
    }

    /**
     * Publica evento de notificação de login
     */
    public void publicarNotificacaoLogin(EmailParaEnvio emailLogin) {
        try {
            logger.info("Publicando notificação de login para: {} (tipo: {})", 
                       emailLogin.getDestinatario(), emailLogin.getTipoEmail());

            publicarEmailParaEnvio(emailLogin);

        } catch (Exception e) {
            logger.error("Erro ao publicar notificação de login: {}", e.getMessage(), e);
            // Não falha o login por causa do email - apenas loga o erro
            logger.warn("Login continuará normalmente, mas notificação de email falhou");
        }
    }

    /**
     * Testa a conectividade com RabbitMQ
     */
    public boolean testarConectividade() {
        try {
            // Envia mensagem de teste
            rabbitTemplate.convertAndSend(
                userExchange,
                "test.connectivity",
                "Teste de conectividade - " + System.currentTimeMillis()
            );
            logger.info("Teste de conectividade com RabbitMQ realizado com sucesso");
            return true;
        } catch (Exception e) {
            logger.error("Falha no teste de conectividade com RabbitMQ: {}", e.getMessage(), e);
            return false;
        }
    }
}
