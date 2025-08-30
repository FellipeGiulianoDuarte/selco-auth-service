// Script de inicialização do MongoDB para o SELCO Auth Service

// Conectar no banco de dados
db = db.getSiblingDB('selco_auth');

// Criar usuário específico para a aplicação
db.createUser({
  user: 'selco_auth_user',
  pwd: 'selco_auth_password',
  roles: [
    {
      role: 'readWrite',
      db: 'selco_auth'
    }
  ]
});

// Criar coleções com validação de schema
db.createCollection('usuarios', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['email', 'senhaHash', 'tipoUsuario', 'status'],
      properties: {
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$'
        },
        senhaHash: {
          bsonType: 'string',
          minLength: 60
        },
        tipoUsuario: {
          bsonType: 'string',
          enum: ['FUNCIONARIO', 'ADMIN']
        },
        status: {
          bsonType: 'string',
          enum: ['ATIVO', 'INATIVO', 'BLOQUEADO']
        },
        dataCriacao: {
          bsonType: 'date'
        },
        dataAtualizacao: {
          bsonType: 'date'
        }
      }
    }
  }
});

db.createCollection('tokens', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['usuarioId', 'refreshToken', 'dataExpiracao'],
      properties: {
        usuarioId: {
          bsonType: 'objectId'
        },
        refreshToken: {
          bsonType: 'string'
        },
        dataExpiracao: {
          bsonType: 'date'
        },
        dataCriacao: {
          bsonType: 'date'
        }
      }
    }
  }
});

db.createCollection('logs_acesso', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['dataHora', 'ip'],
      properties: {
        usuarioId: {
          bsonType: 'objectId'
        },
        dataHora: {
          bsonType: 'date'
        },
        ip: {
          bsonType: 'string'
        },
        userAgent: {
          bsonType: 'string'
        },
        sucesso: {
          bsonType: 'bool'
        },
        motivo: {
          bsonType: 'string'
        }
      }
    }
  }
});

// Criar índices para performance
db.usuarios.createIndex({ "email": 1 }, { unique: true });
db.usuarios.createIndex({ "status": 1 });
db.tokens.createIndex({ "usuarioId": 1 });
db.tokens.createIndex({ "dataExpiracao": 1 }, { expireAfterSeconds: 0 });
db.logs_acesso.createIndex({ "usuarioId": 1 });
db.logs_acesso.createIndex({ "dataHora": -1 });

// Inserir usuário admin padrão para testes
db.usuarios.insertOne({
  email: 'admin@selco.com.br',
  senhaHash: '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeG.XLyq5F8U5Mz5y', // senha: admin123
  tipoUsuario: 'ADMIN',
  status: 'ATIVO',
  dataCriacao: new Date(),
  dataAtualizacao: new Date()
});

print('MongoDB inicializado com sucesso para o SELCO Auth Service!');
