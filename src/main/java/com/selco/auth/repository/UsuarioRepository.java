package com.selco.auth.repository;

import com.selco.auth.model.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositório para a entidade Usuario
 */
@Repository
public interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    /**
     * Busca um usuário pelo email
     */
    Optional<Usuario> findByEmail(String email);
    
    /**
     * Verifica se existe um usuário com o email informado
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca usuários por status
     */
    java.util.List<Usuario> findByStatus(String status);
}
