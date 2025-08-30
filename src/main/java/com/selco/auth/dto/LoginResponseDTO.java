package com.selco.auth.dto;

/**
 * DTO para resposta de login
 * Implementa AUT-13: Endpoint de Login e AUT-15: Geração de Token JWT
 */
public class LoginResponseDTO {

    private boolean sucesso;
    private String mensagem;
    private String accessToken;
    private String refreshToken;
    private String tipoUsuario;
    private long expiresIn; // em segundos

    // Constructors
    public LoginResponseDTO() {}

    public LoginResponseDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public LoginResponseDTO(boolean sucesso, String mensagem, String accessToken, String refreshToken, String tipoUsuario, long expiresIn) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tipoUsuario = tipoUsuario;
        this.expiresIn = expiresIn;
    }

    // Static factory methods
    public static LoginResponseDTO sucesso(String mensagem, String accessToken, String refreshToken, String tipoUsuario, long expiresIn) {
        return new LoginResponseDTO(true, mensagem, accessToken, refreshToken, tipoUsuario, expiresIn);
    }

    public static LoginResponseDTO erro(String mensagem) {
        return new LoginResponseDTO(false, mensagem);
    }

    // Getters and Setters
    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "LoginResponseDTO{" +
                "sucesso=" + sucesso +
                ", mensagem='" + mensagem + '\'' +
                ", accessToken='" + (accessToken != null ? "[TOKEN]" : null) + '\'' +
                ", refreshToken='" + (refreshToken != null ? "[TOKEN]" : null) + '\'' +
                ", tipoUsuario='" + tipoUsuario + '\'' +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
