package com.selco.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço para geração e validação de tokens JWT
 * Implementa AUT-15: Geração do Token JWT
 */
@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-time}")
    private long jwtExpirationTime; // em milissegundos

    @Value("${app.jwt.refresh-expiration-time}")
    private long refreshExpirationTime; // em milissegundos

    /**
     * AUT-15: Gera token JWT com informações do usuário
     */
    public String generateAccessToken(String email, String tipoUsuario, String usuarioId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tipoUsuario", tipoUsuario);
        claims.put("usuarioId", usuarioId);
        
        return createToken(claims, email, jwtExpirationTime);
    }

    /**
     * AUT-16: Gera refresh token
     */
    public String generateRefreshToken(String email) {
        return createToken(new HashMap<>(), email, refreshExpirationTime);
    }

    /**
     * Cria token JWT
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai email do token
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai data de expiração do token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrai tipo de usuário do token
     */
    public String extractTipoUsuario(String token) {
        final Claims claims = extractAllClaims(token);
        return (String) claims.get("tipoUsuario");
    }

    /**
     * Extrai ID do usuário do token
     */
    public String extractUsuarioId(String token) {
        final Claims claims = extractAllClaims(token);
        return (String) claims.get("usuarioId");
    }

    /**
     * Extrai claim específico do token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrai todas as claims do token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifica se o token expirou
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida o token
     */
    public boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            return (tokenEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Erro ao validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtém a chave de assinatura
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Retorna o tempo de expiração do access token em segundos
     */
    public long getAccessTokenExpirationInSeconds() {
        return jwtExpirationTime / 1000;
    }
}
