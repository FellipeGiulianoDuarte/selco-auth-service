package com.selco.auth.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Classe base para testes de integração com TestContainers
 * Implementa AUT-24: Testes de Integração
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    // Container do MongoDB
    @Container
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withExposedPorts(27017);

    // Container do Redis
    @Container
    static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--appendonly", "yes");
    
    static {
        // Força o início dos containers
        mongoDBContainer.start();
        redisContainer.start();
    }

    /**
     * Configura as propriedades dinâmicas para os containers
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        // Configurações do MongoDB TestContainer
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl("selco_auth_test"));
        
        // Configurações do Redis TestContainer
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        
        // Configurações de JWT para testes
        registry.add("app.security.jwt.secret", () -> "test-secret-key-with-at-least-32-characters-for-hmac-sha256");
        registry.add("app.security.jwt.expiration", () -> "86400000"); // 1 dia em ms
        registry.add("app.security.jwt.refresh-expiration", () -> "604800000"); // 7 dias em ms
        
        // Desabilitar RabbitMQ para testes (mock/disable)
        registry.add("spring.rabbitmq.host", () -> "disabled");
        registry.add("spring.rabbitmq.port", () -> "0");
        registry.add("app.rabbitmq.enabled", () -> "false");
        
        // Configurações de logging para testes
        registry.add("logging.level.com.selco.auth", () -> "DEBUG");
        registry.add("logging.level.org.springframework.security", () -> "DEBUG");
    }
}
