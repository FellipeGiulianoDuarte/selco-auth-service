package com.selco.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Entidade Token (Refresh Token) para MongoDB
 */
@Document(collection = "tokens")
public class Token {
    
    @Id
    private String id;
    
    @Indexed
    private String usuarioId;
    
    private String refreshToken;
    
    @Indexed(expireAfterSeconds = 0)
    private LocalDateTime dataExpiracao;
    
    private LocalDateTime dataCriacao;
    
    // Construtores
    public Token() {}
    
    public Token(String usuarioId, String refreshToken, LocalDateTime dataExpiracao) {
        this.usuarioId = usuarioId;
        this.refreshToken = refreshToken;
        this.dataExpiracao = dataExpiracao;
        this.dataCriacao = LocalDateTime.now();
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public LocalDateTime getDataExpiracao() {
        return dataExpiracao;
    }
    
    public void setDataExpiracao(LocalDateTime dataExpiracao) {
        this.dataExpiracao = dataExpiracao;
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    /**
     * Verifica se o token está expirado
     */
    public boolean isExpirado() {
        return LocalDateTime.now().isAfter(this.dataExpiracao);
    }
}
