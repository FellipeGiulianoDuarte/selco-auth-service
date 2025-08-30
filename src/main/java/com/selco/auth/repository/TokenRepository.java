package com.selco.auth.repository;

import com.selco.auth.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade Token (Refresh Tokens)
 */
@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    
    /**
     * Busca um token pelo refresh token
     */
    Optional<Token> findByRefreshToken(String refreshToken);
    
    /**
     * Busca tokens por usuário
     */
    java.util.List<Token> findByUsuarioId(String usuarioId);
    
    /**
     * Remove todos os tokens de um usuário
     */
    void deleteByUsuarioId(String usuarioId);
}
