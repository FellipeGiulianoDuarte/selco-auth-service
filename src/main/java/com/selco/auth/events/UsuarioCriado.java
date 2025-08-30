package com.selco.auth.events;

import java.time.LocalDateTime;

/**
 * Evento publicado quando um usuário é criado com sucesso
 * Usado para notificar outros microserviços sobre novos usuários
 */
public class UsuarioCriado {
    
    private String usuarioId;
    private String email;
    private String nome;
    private String departamento;
    private String cargo;
    private String tipoUsuario;
    private String status;
    private LocalDateTime dataCriacao;
    private String senhaTemporaria; // Para envio por email
    
    // Construtor padrão
    public UsuarioCriado() {}
    
    // Construtor completo
    public UsuarioCriado(String usuarioId, String email, String nome, String departamento, 
                        String cargo, String tipoUsuario, String status, 
                        LocalDateTime dataCriacao, String senhaTemporaria) {
        this.usuarioId = usuarioId;
        this.email = email;
        this.nome = nome;
        this.departamento = departamento;
        this.cargo = cargo;
        this.tipoUsuario = tipoUsuario;
        this.status = status;
        this.dataCriacao = dataCriacao;
        this.senhaTemporaria = senhaTemporaria;
    }

    // Getters e Setters
    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
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
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getSenhaTemporaria() {
        return senhaTemporaria;
    }

    public void setSenhaTemporaria(String senhaTemporaria) {
        this.senhaTemporaria = senhaTemporaria;
    }

    @Override
    public String toString() {
        return "UsuarioCriado{" +
                "usuarioId='" + usuarioId + '\'' +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", departamento='" + departamento + '\'' +
                ", cargo='" + cargo + '\'' +
                ", tipoUsuario='" + tipoUsuario + '\'' +
                ", status='" + status + '\'' +
                ", dataCriacao=" + dataCriacao +
                ", senhaTemporaria='[HIDDEN]'" +
                '}';
    }
}
