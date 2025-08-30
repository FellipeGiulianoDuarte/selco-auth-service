package com.selco.auth.repository;

import com.selco.auth.model.LogAcesso;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository para gerenciamento de logs de acesso
 * Implementa AUT-18: Log de Acessos
 */
@Repository
public interface LogAcessoRepository extends MongoRepository<LogAcesso, String> {
    
    /**
     * Busca logs por usuário ordenados por data
     */
    List<LogAcesso> findByUsuarioIdOrderByDataHoraDesc(String usuarioId);
    
    /**
     * Busca logs por período ordenados por data
     */
    List<LogAcesso> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Busca logs de tentativas de acesso por IP
     */
    List<LogAcesso> findByIpAndDataHoraBetween(String ip, LocalDateTime inicio, LocalDateTime fim);
    
    /**
     * Busca logs de sucesso/falha ordenados por data
     */
    List<LogAcesso> findBySucessoOrderByDataHoraDesc(boolean sucesso);

    /**
     * Busca logs por IP ordenados por data
     */
    List<LogAcesso> findByIpOrderByDataHoraDesc(String ip);

    /**
     * Conta tentativas de login com falha por IP em um período
     */
    @Query("{ 'ip': ?0, 'sucesso': false, 'dataHora': { $gte: ?1, $lte: ?2 } }")
    long countFailedAttemptsByIpAndDateRange(String ip, LocalDateTime inicio, LocalDateTime fim);

    /**
     * Conta tentativas de login com falha por usuário em um período
     */
    @Query("{ 'usuarioId': ?0, 'sucesso': false, 'dataHora': { $gte: ?1, $lte: ?2 } }")
    long countFailedAttemptsByUserAndDateRange(String usuarioId, LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca último acesso bem-sucedido de um usuário
     */
    LogAcesso findFirstByUsuarioIdAndSucessoTrueOrderByDataHoraDesc(String usuarioId);
}
