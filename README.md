# API REST — Armazém de Ingredientes

API REST desenvolvida com Java e Spring Boot para gerenciamento de ingredientes, controle de estoque e rastreamento de movimentações em um armazém.

## 1. Tecnologias utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Bean Validation
- H2 Database
- Maven
- JUnit 5
- Mockito

## 2. Funcionalidades

A aplicação permite:

- Cadastrar ingredientes e consultar seus dados.
- Controlar a quantidade armazenada em cada compartimento.
- Consultar o volume armazenado por tipo de ingrediente.
- Identificar compartimentos disponíveis para novas entradas.
- Identificar compartimentos com estoque disponível para venda.
- Registrar saídas de ingredientes.
- Consultar o histórico de movimentações.
- Aplicar regras de capacidade, compatibilidade de tipos e troca de utilização.

## 3. Regras de negócio

O armazém possui cinco compartimentos fixos, identificados como C1, C2, C3, C4 e C5.

As capacidades máximas dependem do tipo de ingrediente:

| Tipo | Capacidade máxima por compartimento |
|---|---|
| SECOS | 600 kg |
| LIQUIDOS | 500 L |
| REFRIGERADOS | 400 kg |

Regras aplicadas:

1. Um compartimento não pode armazenar tipos diferentes simultaneamente.
2. A quantidade armazenada não pode ultrapassar a capacidade máxima do tipo.
3. Um compartimento esvaziado somente pode receber outro tipo a partir do dia seguinte à sua última utilização.
4. Toda entrada e saída deve registrar uma movimentação no histórico.
5. Não é permitido retirar uma quantidade superior ao saldo disponível.
6. As quantidades devem ser positivas e possuir no máximo três casas decimais.

## 4. Arquitetura

O projeto utiliza uma organização em camadas:

- **Controller:** recebe as requisições HTTP e devolve as respostas da API.
- **Service:** concentra as regras de negócio e coordena as operações.
- **Repository:** realiza o acesso aos dados utilizando Spring Data JPA.
- **Entity:** representa as entidades persistidas no banco de dados.
- **DTO:** define os dados recebidos e retornados pelos endpoints.
- **Exception:** centraliza o tratamento de erros da aplicação.
- **Config:** inicializa os cinco compartimentos do armazém.

### Consistência e concorrência

As operações de entrada e saída utilizam `@Transactional` para manter as alterações de estoque e o registro do histórico na mesma transação.

A entidade `Compartimento` utiliza `@Version` para controle de concorrência otimista, permitindo detectar atualizações realizadas sobre versões desatualizadas do registro.

Conflitos de atualização são tratados com HTTP 409 Conflict.

## 5. Endpoints

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/ingredientes` | Cadastrar ingrediente e registrar entrada |
| GET | `/ingredientes` | Listar ingredientes |
| GET | `/ingredientes/{id}` | Consultar ingrediente por ID |
| GET | `/ingredientes/volume` | Consultar volume total por tipo |
| POST | `/ingredientes/{id}/saida` | Registrar saída de estoque |
| GET | `/compartimentos/disponiveis` | Consultar compartimentos com espaço disponível |
| GET | `/compartimentos/disponiveis-para-venda` | Consultar compartimentos com estoque |
| GET | `/historico` | Consultar histórico de movimentações |

### Exemplo de cadastro

**POST `/ingredientes`**

```json
{
  "nome": "Arroz",
  "tipo": "SECOS",
  "quantidade": 100,
  "compartimentoId": 1,
  "responsavel": "João"
}
```

### Exemplo de saída

**POST `/ingredientes/1/saida`**

```json
{
  "quantidade": 30,
  "responsavel": "João"
}
```

### Consulta de compartimentos

**GET `/compartimentos/disponiveis?quantidade=200&tipo=SECOS`**

Retorna os compartimentos que podem receber a quantidade e o tipo informados.

### Consulta de histórico

**GET `/historico?sortBy=date&order=desc`**

Parâmetros disponíveis:

- `sortBy=date`: ordenação por data.
- `sortBy=compartimento`: ordenação por compartimento.
- `order=asc`: ordem crescente.
- `order=desc`: ordem decrescente.

## 6. Tratamento de erros

A API utiliza tratamento centralizado de exceções com `@RestControllerAdvice`.

| Código | Significado |
|---|---|
| 200 | Operação de consulta ou saída realizada |
| 201 | Ingrediente cadastrado |
| 400 | Requisição inválida ou violação de regra de negócio |
| 404 | Recurso não encontrado |
| 409 | Conflito de atualização concorrente |

As respostas de erro apresentam mensagens para facilitar a identificação do problema.

## 7. Como executar

### Pré-requisitos

- JDK 21
- Terminal ou IDE compatível com Java

O projeto inclui o Maven Wrapper, portanto não exige uma instalação global do Maven.

### Windows

Na raiz do projeto, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

### Linux/macOS

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

`http://localhost:8080`

### Banco de dados

A aplicação utiliza H2 em memória.

Console:

`http://localhost:8080/h2-console`

Credenciais para desenvolvimento:

- JDBC URL: `jdbc:h2:mem:armazemdb`
- Usuário: `sa`
- Senha: vazia

Os dados são recriados quando a aplicação é reiniciada. Os compartimentos C1 a C5 são inicializados automaticamente.

## 8. Testes automatizados

O projeto utiliza JUnit 5, Mockito e testes de integração com Spring Boot e H2.

Os testes incluem:

- Validação da capacidade de armazenamento.
- Bloqueio da mistura de tipos.
- Bloqueio da troca de tipo no mesmo dia.
- Permissão de troca de tipo no dia seguinte.
- Cadastro e registro de entrada.
- Saída e atualização de estoque.
- Bloqueio de saídas com saldo insuficiente.
- Persistência integrada de ingrediente, estoque e histórico.
- Rollback transacional diante de falha simulada.
- Detecção de atualização com versão desatualizada.

Para executar os testes no Windows:

```powershell
.\mvnw.cmd clean test
```

Última execução verificada durante o desenvolvimento: **13 testes executados, nenhuma falha e nenhum erro**.

## 9. Melhorias futuras

- Testes de requisições HTTP simultâneas para aprofundar a validação de concorrência.
- Paginação do histórico e das consultas.
- Consultas agregadas e ordenação diretamente no banco.
- Documentação interativa com OpenAPI/Swagger.
- Interface web para movimentação e consulta do estoque.
