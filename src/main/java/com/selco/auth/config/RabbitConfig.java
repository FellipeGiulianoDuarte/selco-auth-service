package com.selco.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do RabbitMQ para o microserviço de autenticação
 * Define exchanges, queues e bindings necessários
 */
@Configuration
public class RabbitConfig {

    // Exchanges
    @Value("${rabbitmq.exchanges.user:selco.user.exchange}")
    private String userExchange;

    @Value("${rabbitmq.exchanges.email:selco.email.exchange}")
    private String emailExchange;

    // Queues
    @Value("${rabbitmq.queues.user-created:selco.user.created.queue}")
    private String userCreatedQueue;

    @Value("${rabbitmq.queues.email-send:selco.email.send.queue}")
    private String emailSendQueue;

    // Routing Keys
    @Value("${rabbitmq.routing-keys.user-created:user.created}")
    private String userCreatedRoutingKey;

    @Value("${rabbitmq.routing-keys.email-send:email.send}")
    private String emailSendRoutingKey;

    /**
     * Conversor de mensagens para JSON
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template do RabbitMQ com conversor JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    /**
     * Factory para containers de listener
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // ==================== EXCHANGES ====================

    /**
     * Exchange para eventos relacionados a usuários
     */
    @Bean
    public TopicExchange userExchange() {
        return ExchangeBuilder
                .topicExchange(userExchange)
                .durable(true)
                .build();
    }

    /**
     * Exchange para eventos relacionados a emails
     */
    @Bean
    public TopicExchange emailExchange() {
        return ExchangeBuilder
                .topicExchange(emailExchange)
                .durable(true)
                .build();
    }

    // ==================== QUEUES ====================

    /**
     * Queue para eventos de usuário criado
     */
    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder
                .durable(userCreatedQueue)
                .withArgument("x-dead-letter-exchange", userExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "user.created.failed")
                .withArgument("x-message-ttl", 300000) // 5 minutos TTL
                .build();
    }

    /**
     * Queue para eventos de envio de email
     */
    @Bean
    public Queue emailSendQueue() {
        return QueueBuilder
                .durable(emailSendQueue)
                .withArgument("x-dead-letter-exchange", emailExchange + ".dlx")
                .withArgument("x-dead-letter-routing-key", "email.send.failed")
                .withArgument("x-message-ttl", 300000) // 5 minutos TTL
                .build();
    }

    // ==================== DEAD LETTER QUEUES ====================

    /**
     * Dead Letter Exchange para usuários
     */
    @Bean
    public DirectExchange userDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(userExchange + ".dlx")
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Exchange para emails
     */
    @Bean
    public DirectExchange emailDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(emailExchange + ".dlx")
                .durable(true)
                .build();
    }

    /**
     * Dead Letter Queue para usuários
     */
    @Bean
    public Queue userDeadLetterQueue() {
        return QueueBuilder
                .durable(userCreatedQueue + ".dlq")
                .build();
    }

    /**
     * Dead Letter Queue para emails
     */
    @Bean
    public Queue emailDeadLetterQueue() {
        return QueueBuilder
                .durable(emailSendQueue + ".dlq")
                .build();
    }

    // ==================== BINDINGS ====================

    /**
     * Binding para eventos de usuário criado
     */
    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder
                .bind(userCreatedQueue())
                .to(userExchange())
                .with(userCreatedRoutingKey);
    }

    /**
     * Binding para eventos de envio de email
     */
    @Bean
    public Binding emailSendBinding() {
        return BindingBuilder
                .bind(emailSendQueue())
                .to(emailExchange())
                .with(emailSendRoutingKey);
    }

    /**
     * Binding para Dead Letter Queue de usuários
     */
    @Bean
    public Binding userDeadLetterBinding() {
        return BindingBuilder
                .bind(userDeadLetterQueue())
                .to(userDeadLetterExchange())
                .with("user.created.failed");
    }

    /**
     * Binding para Dead Letter Queue de emails
     */
    @Bean
    public Binding emailDeadLetterBinding() {
        return BindingBuilder
                .bind(emailDeadLetterQueue())
                .to(emailDeadLetterExchange())
                .with("email.send.failed");
    }

    // ==================== QUEUES DE TESTE ====================

    /**
     * Queue para testes de conectividade
     */
    @Bean
    public Queue testQueue() {
        return QueueBuilder
                .durable("selco.test.queue")
                .withArgument("x-message-ttl", 60000) // 1 minuto TTL
                .build();
    }

    /**
     * Binding para queue de teste
     */
    @Bean
    public Binding testBinding() {
        return BindingBuilder
                .bind(testQueue())
                .to(userExchange())
                .with("test.#");
    }
}
