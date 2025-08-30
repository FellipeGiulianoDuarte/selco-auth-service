# Dockerfile multi-stage para otimizar o build
FROM maven:3.8-openjdk-17 AS builder

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .

# Baixar dependências (para cache do Docker)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar e empacotar a aplicação
RUN mvn clean package -DskipTests

# Imagem final de runtime
FROM openjdk:17-jdk-slim

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN addgroup --system --gid 1001 selco
RUN adduser --system --uid 1001 --gid 1001 selco

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da aplicação
COPY --from=builder /app/target/*.jar app.jar

# Alterar propriedade dos arquivos
RUN chown -R selco:selco /app

# Mudar para usuário não-root
USER selco

# Expor porta
EXPOSE 8081

# Configurar health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/api/auth/health || exit 1

# Comando para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
