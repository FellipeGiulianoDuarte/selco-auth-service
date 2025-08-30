package com.selco.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler para exceções de segurança (Authentication e Authorization)
 * Implementa AUT-21: Tratamento de Exceções de Segurança
 */
@Component
public class SecurityExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger logger = LoggerFactory.getLogger(SecurityExceptionHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Manipula exceções de autenticação (401 Unauthorized)
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        logger.warn("Tentativa de acesso não autenticado para: {} - {}", 
                    request.getRequestURI(), authException.getMessage());

        String message = determineAuthenticationErrorMessage(authException);
        
        sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                         "Acesso não autorizado", message, request.getRequestURI());
    }

    /**
     * Manipula exceções de autorização (403 Forbidden)
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        
        logger.warn("Tentativa de acesso negado para: {} - {}", 
                    request.getRequestURI(), accessDeniedException.getMessage());

        sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                         "Acesso proibido", 
                         "Usuário não possui permissão para acessar este recurso", 
                         request.getRequestURI());
    }

    /**
     * Determina a mensagem de erro baseada no tipo de exceção de autenticação
     */
    private String determineAuthenticationErrorMessage(AuthenticationException authException) {
        String exceptionName = authException.getClass().getSimpleName();
        
        switch (exceptionName) {
            case "BadCredentialsException":
                return "Credenciais inválidas";
            case "AccountExpiredException":
                return "Conta expirada";
            case "CredentialsExpiredException":
                return "Credenciais expiradas";
            case "DisabledException":
                return "Conta desabilitada";
            case "LockedException":
                return "Conta bloqueada";
            case "InsufficientAuthenticationException":
                return "Token de autenticação ausente ou inválido";
            default:
                return "Falha na autenticação: " + authException.getMessage();
        }
    }

    /**
     * Envia resposta de erro padronizada
     */
    private void sendErrorResponse(HttpServletResponse response, int status, 
                                   String error, String message, String path) throws IOException {
        
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status);
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        
        // Adiciona informações específicas baseadas no status
        if (status == HttpServletResponse.SC_UNAUTHORIZED) {
            errorResponse.put("code", "AUTH_REQUIRED");
            errorResponse.put("details", "É necessário estar autenticado para acessar este recurso");
        } else if (status == HttpServletResponse.SC_FORBIDDEN) {
            errorResponse.put("code", "ACCESS_DENIED");
            errorResponse.put("details", "Permissões insuficientes para realizar esta operação");
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
        
        logger.debug("Enviada resposta de erro: Status {} - {}", status, message);
    }
}
