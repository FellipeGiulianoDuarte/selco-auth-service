package com.selco.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * DTO para resposta de validação de token JWT
 * Implementa AUT-22: Endpoint de Validação de Token
 */
@Schema(description = "Resposta de validação de token")
public class TokenValidationDTO {

    @JsonProperty("valido")
    @Schema(description = "Indica se o token é válido", example = "true")
    private boolean valido;

    @JsonProperty("usuario_id")
    @Schema(description = "ID do usuário proprietário do token", example = "676a1b2c3d4e5f6789012345")
    private String usuarioId;

    @JsonProperty("nome_usuario")
    @Schema(description = "Nome do usuário", example = "João Silva")
    private String nomeUsuario;

    @JsonProperty("email")
    @Schema(description = "Email do usuário", example = "joao.silva@empresa.com")
    private String email;

    @JsonProperty("tipo_usuario")
    @Schema(description = "Tipo do usuário", example = "FUNCIONARIO")
    private String tipoUsuario;

    @JsonProperty("expires_at")
    @Schema(description = "Data e hora de expiração do token", example = "2024-12-24T10:30:00")
    private LocalDateTime expiresAt;

    @JsonProperty("mensagem")
    @Schema(description = "Mensagem adicional sobre a validação", example = "Token válido e ativo")
    private String mensagem;

    @JsonProperty("timestamp")
    @Schema(description = "Timestamp da validação", example = "2024-12-23T14:30:45")
    private LocalDateTime timestamp;

    public TokenValidationDTO() {
        this.timestamp = LocalDateTime.now();
    }

    // Construtor para token válido
    public TokenValidationDTO(String usuarioId, String nomeUsuario, String email, String tipoUsuario, LocalDateTime expiresAt) {
        this();
        this.valido = true;
        this.usuarioId = usuarioId;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.tipoUsuario = tipoUsuario;
        this.expiresAt = expiresAt;
        this.mensagem = "Token válido e ativo";
    }

    // Construtor para token inválido
    public TokenValidationDTO(String mensagem) {
        this();
        this.valido = false;
        this.mensagem = mensagem;
    }

    // Factory methods
    public static TokenValidationDTO valido(String usuarioId, String nomeUsuario, String email, String tipoUsuario, LocalDateTime expiresAt) {
        return new TokenValidationDTO(usuarioId, nomeUsuario, email, tipoUsuario, expiresAt);
    }

    public static TokenValidationDTO invalido(String mensagem) {
        return new TokenValidationDTO(mensagem);
    }

    // Getters e Setters
    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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

    @Override
    public String toString() {
        return "TokenValidationDTO{" +
                "valido=" + valido +
                ", usuarioId='" + usuarioId + '\'' +
                ", nomeUsuario='" + nomeUsuario + '\'' +
                ", email='" + email + '\'' +
                ", tipoUsuario='" + tipoUsuario + '\'' +
                ", expiresAt=" + expiresAt +
                ", mensagem='" + mensagem + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
