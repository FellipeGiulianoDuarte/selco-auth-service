package com.selco.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisição de logout
 * Implementa AUT-17: Endpoint de Logout
 */
@Schema(description = "Dados necessários para realizar logout")
public class LogoutRequestDTO {

    @NotBlank(message = "Token é obrigatório")
    @Schema(description = "Token JWT a ser invalidado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    // Construtores
    public LogoutRequestDTO() {}

    public LogoutRequestDTO(String token) {
        this.token = token;
    }

    // Getters e Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
