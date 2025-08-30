package com.selco.auth.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Entidade Usuario para MongoDB
 */
@Document(collection = "usuarios")
public class Usuario {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String email;
    
    private String senhaHash;
    
    private String tipoUsuario; // FUNCIONARIO, ADMIN
    
    @Indexed
    private String status; // ATIVO, INATIVO, BLOQUEADO
    
    private LocalDateTime dataCriacao;
    
    private LocalDateTime dataAtualizacao;
    
    // Construtores
    public Usuario() {}
    
    public Usuario(String email, String senhaHash, String tipoUsuario, String status) {
        this.email = email;
        this.senhaHash = senhaHash;
        this.tipoUsuario = tipoUsuario;
        this.status = status;
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getSenhaHash() {
        return senhaHash;
    }
    
    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }
    
    public String getTipoUsuario() {
        return tipoUsuario;
    }
    
    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
        this.dataAtualizacao = LocalDateTime.now();
    }
    
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    
    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }
    
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
