package com.selco.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para requisições de login
 * Implementa AUT-13: Endpoint de Login
 */
public class LoginRequestDTO {

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ter um formato válido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    // Constructors
    public LoginRequestDTO() {}

    public LoginRequestDTO(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    @Override
    public String toString() {
        return "LoginRequestDTO{" +
                "email='" + email + '\'' +
                ", senha='[PROTECTED]'" +
                '}';
    }
}
