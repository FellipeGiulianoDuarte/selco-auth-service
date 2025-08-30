package com.selco.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO de resposta para operações de logout
 * Implementa AUT-17: Endpoint de Logout
 */
@Schema(description = "Resposta do logout")
public class LogoutResponseDTO {

    @Schema(description = "Indica se o logout foi realizado com sucesso", example = "true")
    private boolean sucesso;

    @Schema(description = "Mensagem descritiva do resultado", example = "Logout realizado com sucesso")
    private String mensagem;

    @Schema(description = "Timestamp do logout")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Construtores
    public LogoutResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public LogoutResponseDTO(boolean sucesso, String mensagem) {
        this();
        this.sucesso = sucesso;
        this.mensagem = mensagem;
    }

    // Métodos estáticos para criar respostas
    public static LogoutResponseDTO sucesso(String mensagem) {
        return new LogoutResponseDTO(true, mensagem);
    }

    public static LogoutResponseDTO erro(String mensagem) {
        return new LogoutResponseDTO(false, mensagem);
    }

    // Getters e Setters
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
