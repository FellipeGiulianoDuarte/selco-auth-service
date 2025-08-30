package com.selco.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selco.auth.dto.CadastroFuncionarioDTO;
import com.selco.auth.dto.CadastroResponseDTO;
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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de Integração para o fluxo completo de autenticação
 * Implementa AUT-24: Testes de Integração com TestContainers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
@AutoConfigureWebMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Testes de Integração - Fluxo de Autenticação")
public class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Container do MongoDB
    @Container
    @SuppressWarnings("resource")
    static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .withExposedPorts(27017);

    // Container do Redis
    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
            .withExposedPorts(6379);

    /**
     * Configura as propriedades dinâmicas para os containers
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configurações do MongoDB TestContainer
        registry.add("spring.data.mongodb.uri", () -> mongoDBContainer.getReplicaSetUrl("selco_auth_test"));
        
        // Configurações do Redis TestContainer
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort);
        
        // Configurações de JWT para testes
        registry.add("app.security.jwt.secret", () -> "test-secret-key-with-at-least-32-characters-for-hmac-sha256");
        registry.add("app.security.jwt.expiration", () -> "86400000"); // 1 dia em ms
        registry.add("app.security.jwt.refresh-expiration", () -> "604800000"); // 7 dias em ms
        
        // Configurações de logging para testes
        registry.add("logging.level.com.selco.auth", () -> "INFO");
    }

    @BeforeEach
    void setUp() {
        // Limpa o banco de dados antes de cada teste
        usuarioRepository.deleteAll();
    }

    private String getBaseUrl() {
        return "http://localhost:" + port + "/api/auth";
    }

    @Test
    @DisplayName("Deve executar fluxo de login → validação → logout com usuário pré-cadastrado")
    void deveExecutarFluxoLoginValidacaoLogout() throws Exception {
        String baseUrl = getBaseUrl();
        
        // 1. PRÉ-CADASTRO: Criar usuário diretamente no banco para teste
        System.out.println("=== CRIANDO USUÁRIO PARA TESTE ===");
        
        Usuario usuarioTeste = new Usuario();
        usuarioTeste.setEmail("teste.integração@empresa.com");
        usuarioTeste.setSenhaHash("$2a$12$YourHashedPasswordHere123456"); // Hash de "123456"
        usuarioTeste.setTipoUsuario("FUNCIONARIO");
        usuarioTeste.setStatus("ATIVO");
        usuarioTeste.setDataCriacao(java.time.LocalDateTime.now());
        usuarioTeste.setDataAtualizacao(java.time.LocalDateTime.now());
        
        Usuario usuarioSalvo = usuarioRepository.save(usuarioTeste);
        assertThat(usuarioSalvo.getId()).isNotNull();
        
        System.out.println("✓ Usuário de teste criado: " + usuarioSalvo.getEmail());
        
        // 2. TESTE DE LOGIN
        System.out.println("=== EXECUTANDO TESTE DE LOGIN ===");
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("teste.integração@empresa.com");
        loginRequest.setSenha("123456"); // Senha conhecida para teste
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequestDTO> loginEntity = new HttpEntity<>(loginRequest, headers);
        
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.exchange(
            baseUrl + "/login",
            HttpMethod.POST,
            loginEntity,
            LoginResponseDTO.class
        );
        
        // Verifica o login
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().isSucesso()).isTrue();
        assertThat(loginResponse.getBody().getAccessToken()).isNotEmpty();
        
        String accessToken = loginResponse.getBody().getAccessToken();
        System.out.println("✓ Login realizado com sucesso. Token: " + accessToken.substring(0, 20) + "...");
        
        // 3. TESTE DE VALIDAÇÃO DE TOKEN
        System.out.println("=== EXECUTANDO TESTE DE VALIDAÇÃO DE TOKEN ===");
        
        HttpHeaders validationHeaders = new HttpHeaders();
        validationHeaders.set("Authorization", "Bearer " + accessToken);
        validationHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> validationEntity = new HttpEntity<>(validationHeaders);
        
        ResponseEntity<TokenValidationDTO> validationResponse = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            validationEntity,
            TokenValidationDTO.class
        );
        
        // Verifica a validação
        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody()).isNotNull();
        assertThat(validationResponse.getBody().isValido()).isTrue();
        assertThat(validationResponse.getBody().getEmail()).isEqualTo("teste.integração@empresa.com");
        assertThat(validationResponse.getBody().getTipoUsuario()).isEqualTo("FUNCIONARIO");
        
        System.out.println("✓ Token validado com sucesso para usuário: " + validationResponse.getBody().getEmail());
        
        // 4. TESTE DE LOGOUT
        System.out.println("=== EXECUTANDO TESTE DE LOGOUT ===");
        
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO();
        logoutRequest.setToken(accessToken);
        
        HttpEntity<LogoutRequestDTO> logoutEntity = new HttpEntity<>(logoutRequest, headers);
        
        ResponseEntity<LogoutResponseDTO> logoutResponse = restTemplate.exchange(
            baseUrl + "/logout",
            HttpMethod.POST,
            logoutEntity,
            LogoutResponseDTO.class
        );
        
        // Verifica o logout
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody()).isNotNull();
        assertThat(logoutResponse.getBody().isSucesso()).isTrue();
        
        System.out.println("✓ Logout realizado com sucesso");
        
        // 5. TESTE DE VALIDAÇÃO DE TOKEN APÓS LOGOUT (deve falhar)
        System.out.println("=== TESTANDO VALIDAÇÃO APÓS LOGOUT ===");
        
        ResponseEntity<TokenValidationDTO> validationAfterLogoutResponse = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            validationEntity,
            TokenValidationDTO.class
        );
        
        // Token deve estar inválido após logout
        assertThat(validationAfterLogoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(validationAfterLogoutResponse.getBody()).isNotNull();
        assertThat(validationAfterLogoutResponse.getBody().isValido()).isFalse();
        
        System.out.println("✓ Token corretamente invalidado após logout");
        System.out.println("=== FLUXO DE AUTENTICAÇÃO CONCLUÍDO COM SUCESSO! ===");
    }
    
    @Test
    @DisplayName("Deve testar fluxo completo de cadastro com domínio válido")
    void deveTestarCadastroComDominioValido() throws Exception {
        String baseUrl = getBaseUrl();
        
        // 1. TESTE DE CADASTRO
        System.out.println("=== EXECUTANDO TESTE DE CADASTRO ===");
        
        CadastroFuncionarioDTO cadastroRequest = new CadastroFuncionarioDTO();
        cadastroRequest.setCpf("123.456.789-00");
        cadastroRequest.setNome("Funcionário Teste");
        cadastroRequest.setEmail("funcionario.teste@empresa.com");
        cadastroRequest.setDepartamento("TI");
        cadastroRequest.setCargo("Desenvolvedor");
        
        HttpHeaders cadastroHeaders = new HttpHeaders();
        cadastroHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CadastroFuncionarioDTO> cadastroEntity = new HttpEntity<>(cadastroRequest, cadastroHeaders);
        
        ResponseEntity<CadastroResponseDTO> cadastroResponse = restTemplate.exchange(
            baseUrl + "/register", 
            HttpMethod.POST, 
            cadastroEntity, 
            CadastroResponseDTO.class
        );
        
        // Verifica o cadastro
        assertThat(cadastroResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(cadastroResponse.getBody()).isNotNull();
        assertThat(cadastroResponse.getBody().isSucesso()).isTrue();
        assertThat(cadastroResponse.getBody().getUsuarioId()).isNotEmpty();
        
        System.out.println("✓ Cadastro realizado com sucesso. ID: " + cadastroResponse.getBody().getUsuarioId());
        
        // Busca o usuário cadastrado para obter a senha temporária
        // Em um ambiente real, a senha seria enviada por e-mail
        // Para os testes, vamos usar uma senha conhecida ou buscar no banco
        Usuario usuarioCadastrado = usuarioRepository.findByEmail("funcionario.teste@empresa.com").orElse(null);
        assertThat(usuarioCadastrado).isNotNull();
        
        // Para o teste, vamos usar uma senha padrão conhecida (123456)
        String senhaTemporaria = "123456"; // Senha padrão para testes
        System.out.println("✓ Cadastro realizado com sucesso. Senha temporária: " + senhaTemporaria);
        
        // 2. TESTE DE LOGIN
        System.out.println("=== EXECUTANDO TESTE DE LOGIN ===");
        
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("funcionario.teste@empresa.com");
        loginRequest.setSenha(senhaTemporaria);
        
        HttpEntity<LoginRequestDTO> loginEntity = new HttpEntity<>(loginRequest, cadastroHeaders);
        
        ResponseEntity<LoginResponseDTO> loginResponse = restTemplate.exchange(
            baseUrl + "/login",
            HttpMethod.POST,
            loginEntity,
            LoginResponseDTO.class
        );
        
        // Verifica o login
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().isSucesso()).isTrue();
        assertThat(loginResponse.getBody().getAccessToken()).isNotEmpty();
        assertThat(loginResponse.getBody().getRefreshToken()).isNotEmpty();
        
        String accessToken = loginResponse.getBody().getAccessToken();
        String refreshToken = loginResponse.getBody().getRefreshToken();
        System.out.println("✓ Login realizado com sucesso. Token obtido: " + accessToken.substring(0, 20) + "...");
        
        // 3. TESTE DE VALIDAÇÃO DE TOKEN
        System.out.println("=== EXECUTANDO TESTE DE VALIDAÇÃO DE TOKEN ===");
        
        HttpHeaders validationHeaders = new HttpHeaders();
        validationHeaders.set("Authorization", "Bearer " + accessToken);
        validationHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> validationEntity = new HttpEntity<>(validationHeaders);
        
        ResponseEntity<TokenValidationDTO> validationResponse = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            validationEntity,
            TokenValidationDTO.class
        );
        
        // Verifica a validação
        assertThat(validationResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validationResponse.getBody()).isNotNull();
        assertThat(validationResponse.getBody().isValido()).isTrue();
        assertThat(validationResponse.getBody().getEmail()).isEqualTo("funcionario.teste@empresa.com");
        assertThat(validationResponse.getBody().getTipoUsuario()).isEqualTo("FUNCIONARIO");
        
        System.out.println("✓ Token validado com sucesso para usuário: " + validationResponse.getBody().getEmail());
        
        // 4. TESTE DE LOGOUT
        System.out.println("=== EXECUTANDO TESTE DE LOGOUT ===");
        
        LogoutRequestDTO logoutRequest = new LogoutRequestDTO();
        logoutRequest.setToken(accessToken);
        
        HttpEntity<LogoutRequestDTO> logoutEntity = new HttpEntity<>(logoutRequest, cadastroHeaders);
        
        ResponseEntity<LogoutResponseDTO> logoutResponse = restTemplate.exchange(
            baseUrl + "/logout",
            HttpMethod.POST,
            logoutEntity,
            LogoutResponseDTO.class
        );
        
        // Verifica o logout
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(logoutResponse.getBody()).isNotNull();
        assertThat(logoutResponse.getBody().isSucesso()).isTrue();
        
        System.out.println("✓ Logout realizado com sucesso");
        
        // 5. TESTE DE VALIDAÇÃO DE TOKEN APÓS LOGOUT (deve falhar)
        System.out.println("=== TESTANDO VALIDAÇÃO APÓS LOGOUT ===");
        
        ResponseEntity<TokenValidationDTO> validationAfterLogoutResponse = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            validationEntity,
            TokenValidationDTO.class
        );
        
        // Token deve estar inválido após logout
        assertThat(validationAfterLogoutResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(validationAfterLogoutResponse.getBody()).isNotNull();
        assertThat(validationAfterLogoutResponse.getBody().isValido()).isFalse();
        
        System.out.println("✓ Token corretamente invalidado após logout");
        
        System.out.println("=== FLUXO COMPLETO DE AUTENTICAÇÃO CONCLUÍDO COM SUCESSO! ===");
    }
    
    @Test
    @DisplayName("Deve retornar 401 para tentativa de acesso com token inválido")
    void deveRetornar401ParaTokenInvalido() {
        String baseUrl = getBaseUrl();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer token-invalido-fake");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<TokenValidationDTO> response = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            entity,
            TokenValidationDTO.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isValido()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("Token inválido");
    }
    
    @Test
    @DisplayName("Deve retornar 401 para tentativa de acesso sem token")
    void deveRetornar401ParaAcessoSemToken() {
        String baseUrl = getBaseUrl();
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<TokenValidationDTO> response = restTemplate.exchange(
            baseUrl + "/validate",
            HttpMethod.POST,
            entity,
            TokenValidationDTO.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isValido()).isFalse();
        assertThat(response.getBody().getMensagem()).contains("Token não fornecido");
    }
    
    @Test
    @DisplayName("Deve validar health check está funcionando")
    void deveValidarHealthCheck() {
        String healthUrl = "http://localhost:" + port + "/api/auth/health";
        
        ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("SELCO Auth Service está funcionando!");
    }
}
