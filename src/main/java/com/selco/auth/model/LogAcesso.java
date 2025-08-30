package com.selco.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Entidade LogAcesso para MongoDB
 */
@Document(collection = "logs_acesso")
public class LogAcesso {
    
    @Id
    private String id;
    
    @Indexed
    private String usuarioId;
    
    @Indexed
    private LocalDateTime dataHora;
    
    private String ip;
    
    private String userAgent;
    
    private boolean sucesso;
    
    private String motivo; // Motivo da falha (se aplicável)
    
    // Construtores
    public LogAcesso() {}
    
    public LogAcesso(String usuarioId, String ip, String userAgent, boolean sucesso) {
        this.usuarioId = usuarioId;
        this.ip = ip;
        this.userAgent = userAgent;
        this.sucesso = sucesso;
        this.dataHora = LocalDateTime.now();
    }
    
    public LogAcesso(String usuarioId, String ip, String userAgent, boolean sucesso, String motivo) {
        this(usuarioId, ip, userAgent, sucesso);
        this.motivo = motivo;
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
    
    public LocalDateTime getDataHora() {
        return dataHora;
    }
    
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }
    
    public String getIp() {
        return ip;
    }
    
    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public boolean isSucesso() {
        return sucesso;
    }
    
    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
