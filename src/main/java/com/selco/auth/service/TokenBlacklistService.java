package com.selco.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

/**
 * Serviço para gerenciar blacklist de tokens JWT no Redis
 * Implementa AUT-17: Invalidação de tokens no logout
 */
@Service
public class TokenBlacklistService {

    private static final Logger logger = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtService jwtService;

    /**
     * Adiciona um token à blacklist
     * @param token Token JWT a ser invalidado
     * @param expiration Data de expiração do token
     */
    public void blacklistToken(String token, Date expiration) {
        try {
            String key = BLACKLIST_PREFIX + token;
            
            // Calcula o TTL baseado na expiração do token
            long ttlSeconds = calculateTTL(expiration);
            
            if (ttlSeconds > 0) {
                // Armazena no Redis com TTL automático
                redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(ttlSeconds));
                logger.info("Token adicionado à blacklist com TTL de {} segundos", ttlSeconds);
            } else {
                logger.warn("Token já expirado, não foi adicionado à blacklist");
            }
        } catch (Exception e) {
            logger.error("Erro ao adicionar token à blacklist: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao invalidar token", e);
        }
    }

    /**
     * Verifica se um token está na blacklist
     * @param token Token JWT a ser verificado
     * @return true se o token está na blacklist
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            logger.error("Erro ao verificar blacklist: {}", e.getMessage(), e);
            // Em caso de erro, considera o token como válido para não bloquear usuários
            return false;
        }
    }

    /**
     * Invalida um token pelo seu valor
     * @param token Token JWT a ser invalidado
     */
    public void invalidateToken(String token) {
        try {
            // Extrai a data de expiração do token
            Date expiration = jwtService.extractExpiration(token);
            blacklistToken(token, expiration);
            
            logger.info("Token invalidado com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao invalidar token: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao realizar logout", e);
        }
    }

    /**
     * Calcula o TTL em segundos até a expiração do token
     */
    private long calculateTTL(Date expiration) {
        long now = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        return Math.max(0, (expirationTime - now) / 1000);
    }

    /**
     * Remove todos os tokens da blacklist (método para limpeza/manutenção)
     */
    public void clearBlacklist() {
        try {
            String pattern = BLACKLIST_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("Blacklist limpa: {} tokens removidos", keys.size());
            }
        } catch (Exception e) {
            logger.error("Erro ao limpar blacklist: {}", e.getMessage(), e);
        }
    }

    /**
     * Conta quantos tokens estão na blacklist
     */
    public long getBlacklistSize() {
        try {
            String pattern = BLACKLIST_PREFIX + "*";
            var keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("Erro ao contar tokens na blacklist: {}", e.getMessage(), e);
            return 0;
        }
    }
}
