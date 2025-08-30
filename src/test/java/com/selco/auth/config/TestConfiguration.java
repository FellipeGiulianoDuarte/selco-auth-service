package com.selco.auth.config;

import org.mockito.Mockito;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import com.selco.auth.events.EmailParaEnvio;
import com.selco.auth.events.UsuarioCriado;
import com.selco.auth.service.EventPublisherService;

import static org.mockito.Mockito.*;

/**
 * Configuração para testes que substitui dependências externas por mocks
 */
@Configuration
@Profile("test")
public class TestConfiguration {

    /**
     * Mock do ConnectionFactory para testes
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "false", matchIfMissing = true)
    public ConnectionFactory mockConnectionFactory() {
        return Mockito.mock(ConnectionFactory.class);
    }

    /**
     * Mock do RabbitTemplate para testes
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "false", matchIfMissing = true)
    public RabbitTemplate mockRabbitTemplate() {
        return Mockito.mock(RabbitTemplate.class);
    }

    /**
     * Mock do EventPublisherService para testes
     * Evita a necessidade de RabbitMQ nos testes
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.rabbitmq.enabled", havingValue = "false", matchIfMissing = true)
    public EventPublisherService mockEventPublisherService() {
        EventPublisherService mockService = mock(EventPublisherService.class);
        
        // Configurar comportamento do mock para não fazer nada nos métodos
        doNothing().when(mockService).publicarEmailParaEnvio(any(EmailParaEnvio.class));
        doNothing().when(mockService).publicarUsuarioCriado(any(UsuarioCriado.class));
        doNothing().when(mockService).publicarEventosCadastro(any(UsuarioCriado.class), any(EmailParaEnvio.class));
        doNothing().when(mockService).publicarNotificacaoLogin(any(EmailParaEnvio.class));
        
        return mockService;
    }
}
