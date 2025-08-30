package com.selco.auth.config;

import com.selco.auth.exception.SecurityExceptionHandler;
import com.selco.auth.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuração de segurança do Spring Security
 * Implementa AUT-19: Configuração do Spring Security e AUT-20: Filtro JWT e AUT-21: Exception Handler
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private SecurityExceptionHandler securityExceptionHandler;

    /**
     * Configuração da cadeia de filtros de segurança
     * AUT-19: SecurityFilterChain para proteger endpoints
     * AUT-20: Integração com filtro JWT
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Desabilita CSRF para APIs REST
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless para JWT
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos - não requerem autenticação
                        .requestMatchers(
                                "/auth/health",               // Health check (sem /api prefix no contexto)
                                "/auth/register",             // Cadastro de funcionário
                                "/auth/login",                // Login
                                "/error",                     // Página de erro do Spring Boot
                                "/actuator/health",           // Actuator health check
                                "/actuator/**",               // Todos os endpoints do actuator
                                "/api-docs/**",               // Swagger API docs
                                "/swagger-ui/**",             // Swagger UI
                                "/swagger-ui.html",           // Swagger UI
                                "/v3/api-docs/**"             // OpenAPI docs
                        ).permitAll()
                        // Todos os outros endpoints requerem autenticação
                        .anyRequest().authenticated()
                )
                // Adiciona o filtro JWT antes do filtro de autenticação padrão
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // Configura handlers de exceção de segurança
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(securityExceptionHandler)
                        .accessDeniedHandler(securityExceptionHandler)
                );

        return http.build();
    }

    /**
     * Bean do BCryptPasswordEncoder com cost 12
     * AUT-10: Configuração do BCrypt com cost 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
