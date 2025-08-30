package com.selco.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SelcoAuthServiceApplicationTests {

    @Test
    void contextLoads() {
        // Teste básico para verificar se o contexto da aplicação carrega corretamente
        // Com o profile 'test' ativo, usa as configurações de teste
    }
}
