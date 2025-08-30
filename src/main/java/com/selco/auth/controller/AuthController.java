package com.selco.auth.controller;

import com.selco.auth.dto.CadastroFuncionarioDTO;
import com.selco.auth.dto.CadastroResponseDTO;
import com.selco.auth.dto.LoginRequestDTO;
import com.selco.auth.dto.LoginResponseDTO;
import com.selco.auth.dto.LogoutRequestDTO;
import com.selco.auth.dto.LogoutResponseDTO;
import com.selco.auth.dto.TokenValidationDTO;
import com.selco.auth.service.AuthService;
import com.selco.auth.service.CadastroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para endpoints de autenticação
 * Implementa RF01 e RF02 do sistema SELCO
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e cadastro de usuários")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private CadastroService cadastroService;

    @Autowired
    private AuthService authService;

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Verifica se o serviço de autenticação está funcionando")
    @ApiResponse(responseCode = "200", description = "Serviço funcionando corretamente")
    public String health() {
        return "Auth Service is running!";
    }

    /**
     * AUT-06: Endpoint de cadastro de funcionário
     * Implementa RF01: Autocadastro de Funcionário
     */
    @PostMapping("/register")
    @Operation(summary = "Cadastro de Funcionário", description = "Realiza o autocadastro de um funcionário na plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Funcionário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "409", description = "Usuário já existe ou domínio de email inválido"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<CadastroResponseDTO> cadastrarFuncionario(
            @Valid @RequestBody CadastroFuncionarioDTO cadastroDTO) {
        
        logger.info("Recebida requisição de cadastro para: {}", cadastroDTO.getEmail());

        try {
            CadastroResponseDTO response = cadastroService.cadastrarFuncionario(cadastroDTO);
            
            if (response.isSucesso()) {
                logger.info("Cadastro realizado com sucesso para: {}", cadastroDTO.getEmail());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                logger.warn("Falha no cadastro para {}: {}", cadastroDTO.getEmail(), response.getMensagem());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
        } catch (Exception e) {
            logger.error("Erro inesperado no cadastro: {}", e.getMessage(), e);
            CadastroResponseDTO errorResponse = CadastroResponseDTO.erro("Erro interno do servidor");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * AUT-13: Endpoint de login
     * Implementa RF02: Login de Funcionário
     */
    @PostMapping("/login")
    @Operation(summary = "Login de Funcionário", description = "Realiza o login de um funcionário na plataforma")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas"),
            @ApiResponse(responseCode = "403", description = "Usuário inativo ou bloqueado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LoginResponseDTO> realizarLogin(
            @Valid @RequestBody LoginRequestDTO loginRequest,
            HttpServletRequest request) {
        
        logger.info("Recebida requisição de login para: {}", loginRequest.getEmail());

        try {
            // Extrair informações da requisição para auditoria
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);

            LoginResponseDTO response = authService.realizarLogin(loginRequest, userAgent, ipAddress);
            
            if (response.isSucesso()) {
                logger.info("Login realizado com sucesso para: {}", loginRequest.getEmail());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Falha no login para {}: {}", loginRequest.getEmail(), response.getMensagem());
                // Determinar código de status baseado na mensagem de erro
                HttpStatus status = determinarStatusErroLogin(response.getMensagem());
                return ResponseEntity.status(status).body(response);
            }
        } catch (Exception e) {
            logger.error("Erro inesperado no login: {}", e.getMessage(), e);
            LoginResponseDTO errorResponse = LoginResponseDTO.erro("Erro interno do servidor. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Extrai o endereço IP real do cliente considerando proxies
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    /**
     * Determina o código de status HTTP baseado na mensagem de erro
     */
    private HttpStatus determinarStatusErroLogin(String mensagem) {
        if (mensagem.contains("Credenciais inválidas")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (mensagem.contains("inativo") || mensagem.contains("bloqueado")) {
            return HttpStatus.FORBIDDEN;
        } else {
            return HttpStatus.BAD_REQUEST;
        }
    }

    /**
     * AUT-17: Endpoint de logout
     * Implementa invalidação de token JWT
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout de Usuário", description = "Realiza o logout invalidando o token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Token inválido ou malformado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<LogoutResponseDTO> realizarLogout(
            @Valid @RequestBody LogoutRequestDTO logoutRequest,
            HttpServletRequest request) {
        
        logger.info("Recebida requisição de logout");

        try {
            // Extrair informações da requisição para auditoria
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = getClientIpAddress(request);

            LogoutResponseDTO response = authService.realizarLogout(logoutRequest, userAgent, ipAddress);
            
            if (response.isSucesso()) {
                logger.info("Logout realizado com sucesso");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Falha no logout: {}", response.getMensagem());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            logger.error("Erro inesperado no logout: {}", e.getMessage(), e);
            LogoutResponseDTO errorResponse = LogoutResponseDTO.erro("Erro interno do servidor. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * AUT-22: Endpoint de validação de token
     * Verifica se um token JWT é válido e retorna informações do usuário
     */
    @PostMapping("/validate")
    @Operation(summary = "Validar Token JWT", description = "Valida um token JWT e retorna informações do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou expirado"),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    public ResponseEntity<TokenValidationDTO> validarToken(
            HttpServletRequest request) {
        
        logger.info("Recebida requisição de validação de token");

        try {
            // Extrair token do cabeçalho Authorization
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Token não fornecido ou formato inválido");
                TokenValidationDTO response = TokenValidationDTO.invalido("Token não fornecido ou formato inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            String token = authHeader.substring(7); // Remove "Bearer "
            TokenValidationDTO response = authService.validarToken(token);
            
            if (response.isValido()) {
                logger.info("Token validado com sucesso para usuário: {}", response.getUsuarioId());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Token inválido: {}", response.getMensagem());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Erro inesperado na validação de token: {}", e.getMessage(), e);
            TokenValidationDTO errorResponse = TokenValidationDTO.invalido("Erro interno do servidor. Tente novamente mais tarde.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
