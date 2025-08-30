package com.selco.auth.dto;

/**
 * DTO para resposta de cadastro de funcionário
 */
public class CadastroResponseDTO {

    private boolean sucesso;
    private String mensagem;
    private String usuarioId;

    // Constructors
    public CadastroResponseDTO() {}

    public CadastroResponseDTO(boolean sucesso, String mensagem) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    public CadastroResponseDTO(boolean sucesso, String mensagem, String usuarioId) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.usuarioId = usuarioId;
    }

    // Static factory methods
    public static CadastroResponseDTO sucesso(String mensagem, String usuarioId) {
        return new CadastroResponseDTO(true, mensagem, usuarioId);
    }

    public static CadastroResponseDTO erro(String mensagem) {
        return new CadastroResponseDTO(false, mensagem);
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

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return "CadastroResponseDTO{" +
                "sucesso=" + sucesso +
                ", mensagem='" + mensagem + '\'' +
                ", usuarioId='" + usuarioId + '\'' +
                '}';
    }
}
