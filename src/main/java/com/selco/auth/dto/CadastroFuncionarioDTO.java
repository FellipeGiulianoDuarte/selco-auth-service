package com.selco.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisições de cadastro de funcionário
 * Conforme especificado no RF01
 */
public class CadastroFuncionarioDTO {

    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{11}", 
             message = "CPF deve estar no formato XXX.XXX.XXX-XX ou conter 11 dígitos")
    private String cpf;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail deve ter um formato válido")
    private String email;

    @NotBlank(message = "Departamento é obrigatório")
    @Size(min = 2, max = 50, message = "Departamento deve ter entre 2 e 50 caracteres")
    private String departamento;

    @NotBlank(message = "Cargo é obrigatório")
    @Size(min = 2, max = 50, message = "Cargo deve ter entre 2 e 50 caracteres")
    private String cargo;

    // Constructors
    public CadastroFuncionarioDTO() {}

    public CadastroFuncionarioDTO(String cpf, String nome, String email, String departamento, String cargo) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.departamento = departamento;
        this.cargo = cargo;
    }

    // Getters and Setters
    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return "CadastroFuncionarioDTO{" +
                "cpf='" + cpf + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", departamento='" + departamento + '\'' +
                ", cargo='" + cargo + '\'' +
                '}';
    }
}
