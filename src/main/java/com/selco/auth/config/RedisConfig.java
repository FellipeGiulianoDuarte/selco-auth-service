package com.selco.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuração do Redis para cache e blacklist de tokens
 * Implementa AUT-17: Invalidação de tokens no logout
 */
@Configuration
public class RedisConfig {

    /**
     * Configura RedisTemplate para operações string-string
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Configuração dos serializadores
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        
        // Chaves como string
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        
        // Valores como string
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * RedisTemplate genérico para outros tipos de dados se necessário
     */
    @Bean
    public RedisTemplate<String, Object> redisObjectTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));
        
        template.afterPropertiesSet();
        return template;
    }
}
