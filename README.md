# SELCO - Microsserviço de Autenticação (`selco-auth-service`)

Este microsserviço é o pilar central de segurança para a plataforma de E-learning Corporativo SELCO. Sua principal responsabilidade é gerenciar a identidade e o acesso dos usuários, incluindo o autocadastro de funcionários, o processo de login/logout via JWT, a renovação de tokens e o registro de logs de acesso.

Ele opera como o portão de entrada para o ecossistema, garantindo que todas as requisições para outros serviços sejam devidamente autenticadas.

## Stack Técnica (Conforme Especificado)

  * **Linguagem/Framework:** Java 17+ com Spring Boot 3+
  * **Banco de Dados:** MongoDB 7+ (para armazenar dados de usuários, tokens e logs)
  * **Mensageria:** RabbitMQ 3.11+ (para comunicação assíncrona com o serviço de notificações)
  * **Containerização:** Docker e Docker Compose
  * **Documentação da API:** Swagger / OpenAPI 3.0
  * **Testes:** JUnit 5

## Requisitos Funcionais (RF)

### RF01: Autocadastro de Funcionário

  - [ ] Permitir que um funcionário se registre na plataforma informando CPF, Nome, E-mail, Departamento e Cargo.
  - [ ] O serviço deve validar se o domínio do e-mail informado pertence à empresa (o domínio deve ser configurável).
  - [ ] Após o cadastro, uma senha numérica de 6 dígitos deve ser gerada e enviada para o e-mail do usuário (via evento para o `selco-notifications-service`).
  - [ ] O status inicial do usuário na base de autenticação deve ser `ATIVO`.

### RF02: Login e Logout

  - [ ] Prover um endpoint de login que recebe e-mail e senha.
  - [ ] Em caso de sucesso, gerar um JSON Web Token (JWT) com tempo de expiração de 8 horas.
  - [ ] Implementar um mecanismo de refresh token para renovar a sessão do usuário sem exigir novas credenciais.
  - [ ] Prover funcionalidade de logout (ex: adicionar token a uma blacklist até sua expiração).
  - [ ] Registrar todos os acessos (sucesso e falha) em uma coleção de logs, contendo data/hora, endereço IP e `user-agent`.

### RF18: Gerenciamento de Usuários (Parcial)

  - [ ] Prover um endpoint para administradores solicitarem a redefinição de senha de um usuário.
  - [ ] O fluxo de reset deve gerar uma nova senha e disparar um evento para o `selco-notifications-service` para notificar o usuário por e-mail.

## Requisitos Não-Funcionais (RNF)

#### Segurança

  - **Criptografia de Senha:** As senhas devem ser armazenadas utilizando o algoritmo `bcrypt` com um custo (cost factor) de 12.
  - **Tokens JWT:** Os tokens devem ser assinados com o algoritmo `SHA256` e utilizar um `SALT` (segredo) personalizado, armazenado de forma segura em variáveis de ambiente.
  - **Comunicação:** A comunicação em ambiente de produção deve ser obrigatoriamente via HTTPS.

#### Arquitetura e Qualidade

  - **API Design:** A API deve seguir o Nível 2 do Modelo de Maturidade de Richardson (uso correto de verbos HTTP e recursos).
  - **Comunicação entre Serviços:** A comunicação com outros serviços (como Notificações) deve ser feita de forma assíncrona utilizando mensageria (RabbitMQ).
  - **Consumo da API:** O serviço não deve ser consumido diretamente pelo frontend, mas sim através de um API Gateway, que centralizará o roteamento e a validação inicial do token.
  - **Dados Sensíveis:** Dados críticos (como JWTs) não devem ser armazenados em `localStorage` ou `sessionStorage` no frontend.

#### Performance e Escalabilidade

  - **Stateless:** O serviço deve ser stateless, não guardando estado de sessão em memória, para permitir escalabilidade horizontal.

## Endpoints da API

| Método | Rota                     | Descrição                                                              |
| :----- | :----------------------- | :--------------------------------------------------------------------- |
| `POST` | `/api/auth/register`     | Realiza o autocadastro de um novo funcionário.                         |
| `POST` | `/api/auth/login`        | Autentica um usuário e retorna um par de tokens (acesso e refresh).    |
| `POST` | `/api/auth/refresh`      | Recebe um refresh token válido e retorna um novo access token.         |
| `POST` | `/api/auth/logout`       | Invalida o token JWT atual do usuário (ex: adicionando à blacklist).    |
| `POST` | `/api/admin/reset-password` | (Rota de Admin) Dispara o fluxo de redefinição de senha para um usuário. |

## Como Executar Localmente

1.  Clone este repositório:

    ```bash
    git clone <url-do-repositorio>
    cd selco-auth-service
    ```

2.  Configure as variáveis de ambiente. Renomeie o arquivo `.env.example` para `.env` e preencha com as informações necessárias (URI do MongoDB, segredo JWT, etc.).

3.  Suba os containers utilizando o Docker Compose:

    ```bash
    docker-compose up -d
    ```

4.  A API estará disponível em `http://localhost:8081` (ou a porta que for configurada).

5.  A documentação interativa do Swagger/OpenAPI poderá ser acessada em `http://localhost:8081/swagger-ui.html`.