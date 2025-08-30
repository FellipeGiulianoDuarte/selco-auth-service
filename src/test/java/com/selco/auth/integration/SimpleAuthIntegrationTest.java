package com.selco.auth.integration;

import com.selco.auth.dto.LoginRequestDTO;
import com.selco.auth.dto.LoginResponseDTO;
import com.selco.auth.dto.LogoutRequestDTO;
import com.selco.auth.dto.LogoutResponseDTO;
import com.selco.auth.dto.TokenValidationDTO;
import com.selco.auth.model.Usuario;
import com.selco.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de Integração simplificados para autenticação
 * Implementa AUT-24: Testes de Integração com TestContainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testes de Integração Simplificados - Autenticação")
public class SimpleAuthIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @Test
    @DisplayName("Fluxo: Login → Validação → Logout")
    void fluxoLoginValidacaoLogout() {
        System.out.println("\n=== INICIANDO TESTE DE INTEGRAÇÃO COMPLETO ===");
        
        // PRÉ-CONDIÇÃO: Criar usuário teste
        Usuario usuario = criarUsuarioTeste();
        System.out.println("✓ Usuário de teste criado: " + usuario.getEmail());
        
        String baseUrl = getBaseUrl();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // 1. TESTE DE LOGIN
        System.out.println("\n--- TESTANDO LOGIN ---");
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("teste.auth@empresa.com");
        loginRequest.setSenha("senha123");
        
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.exchange(
            baseUrl + "/login", HttpMethod.POST, 
            new HttpEntity<>(loginRequest, headers), 
            LoginResponseDTO.class
        );
        
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().isSucesso()).isTrue();
        assertThat(loginResponse.getBody().getAccessToken()).isNotEmpty();
        
        String token = loginResponse.getBody().getAccessToken();
        System.out.println("✓ Login realizado com sucesso");
        
        // 2. TESTE DE VALIDAÇÃO
        System.out.println("\n--- TESTANDO VALIDAÇÃO DE TOKEN ---");
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.set("Authorization", "Bearer " + token);
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        
        ResponseEntity<TokenValidationDTO> validationResponse = restTemplate.exchange(
            baseUrl + "/validate", HttpMethod.POST,
            new HttpEntity<>(authHeaders),
            TokenValidationDTO.class
        );
        
        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody().isValido()).isTrue();
        assertThat(validationResponse.getBody().getEmail()).isEqualTo("teste.auth@empresa.com");
        
        System.out.println("✓ Token validado com sucesso");
        
        // 3. TESTE DE LOGOUT
        System.out.println("\n--- TESTANDO LOGOUT ---");
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO();
        logoutRequest.setToken(token);
        
        // Adicionar o token no header Authorization para logout
        headers.set("Authorization", "Bearer " + token);
        
        ResponseEntity<LogoutResponseDTO> logoutResponse = restTemplate.exchange(
            baseUrl + "/logout", HttpMethod.POST,
            new HttpEntity<>(logoutRequest, headers),
            LogoutResponseDTO.class
        );
        
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody().isSucesso()).isTrue();
        
        System.out.println("✓ Logout realizado com sucesso");
        
        // 4. TESTE DE TOKEN INVÁLIDO APÓS LOGOUT
        System.out.println("\n--- TESTANDO TOKEN APÓS LOGOUT ---");
        ResponseEntity<TokenValidationDTO> invalidResponse = restTemplate.exchange(
            baseUrl + "/validate", HttpMethod.POST,
            new HttpEntity<>(authHeaders),
            TokenValidationDTO.class
        );
        
        assertThat(invalidResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(invalidResponse.getBody().isValido()).isFalse();
        
        System.out.println("✓ Token invalidado corretamente após logout");
        System.out.println("\n=== TESTE DE INTEGRAÇÃO CONCLUÍDO COM SUCESSO! ===\n");
    }
    
    @Test
    @DisplayName("Health Check")
    void testHealthCheck() {
        String healthUrl = "http://localhost:" + port + "/api/auth/health";
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Auth Service is running!");
        
        System.out.println("✓ Health check funcionando");
    }
    
    @Test
    @DisplayName("Acesso não autorizado")
    void testUnauthorizedAccess() {
        String baseUrl = getBaseUrl();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Tentativa de acesso sem token deve retornar 401
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/validate", HttpMethod.POST,
                new HttpEntity<>("{}", headers),
                String.class
            );
            // Se chegou aqui, o status deve ser 401
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        } catch (HttpClientErrorException e) {
            // Captura a exceção e verifica se é 401
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }
        
        System.out.println("✓ Acesso não autorizado detectado corretamente");
    }
    
    private Usuario criarUsuarioTeste() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste.auth@empresa.com");
        usuario.setSenhaHash(passwordEncoder.encode("senha123"));
        usuario.setTipoUsuario("FUNCIONARIO");
        usuario.setStatus("ATIVO");
        usuario.setDataCriacao(LocalDateTime.now());
        usuario.setDataAtualizacao(LocalDateTime.now());
        
        return usuarioRepository.save(usuario);
    }
}
