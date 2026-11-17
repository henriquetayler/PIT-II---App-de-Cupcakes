# 🧁 Cupcakes Gourmet — PIT II

Loja virtual de cupcakes gourmet, desenvolvida a partir da especificação ágil da PIT I (15 histórias de usuário).

- **Back-end:** Java 17+ com Spring Boot 3 (padrão **MVC**)
- **Front-end:** HTML + CSS renderizados no servidor com Thymeleaf
- **Banco de dados:** MySQL / MariaDB, gerenciado pelo **HeidiSQL**
- **Testes:** JUnit 5 + Mockito (rodam sem banco)

## Como executar

### 1. Pré-requisitos
- JDK 17 ou superior (`java -version`)
- Maven (`mvn -v`) **ou** uma IDE com Maven embutido (IntelliJ IDEA, Eclipse, VS Code com *Extension Pack for Java*)
- MySQL 8 (ou MariaDB 10.4+) em execução, com acesso pelo HeidiSQL

### 2. Criar o banco no HeidiSQL
1. Abra o HeidiSQL e conecte no seu servidor.
2. Aba **Consulta** → abra `sql/01_schema.sql` → **F9**.
3. Atualize a lista de bancos: deve aparecer `cupcakes_db` com 7 tabelas.
4. **Já tinha o banco da versão anterior?** Rode também `sql/02_cupom.sql` antes de iniciar o sistema.

### 3. Configurar a conexão
Em `src/main/resources/application.properties` confira `spring.datasource.url`. Usuário e senha vêm de variáveis de ambiente (padrão: usuário `root`, senha `root`; porta 3307 na URL; ajuste para o seu MySQL):

```bash
# Windows (PowerShell)
$env:DB_USER="root"; $env:DB_PASSWORD="sua_senha"
# Linux / macOS
export DB_USER=root DB_PASSWORD=sua_senha
```

### 4. Rodar
```bash
mvn spring-boot:run
```
Abra http://localhost:8080

Na primeira execução são criados automaticamente: o administrador, 5 categorias e 6 cupcakes de exemplo.

| Perfil | E-mail | Senha |
|---|---|---|
| Administrador | admin@cupcakes.com | Admin@123 |
| Cliente | crie pela tela **Cadastrar** | — |

### 5. Testes
```bash
mvn test
```

## Estrutura (MVC)

```
src/main/java/br/com/cupcakes
├── model/        Entidades (Usuario, Cupcake, Categoria, Pedido, ItemPedido, Cupom) e enums
├── repository/   Acesso ao banco (Spring Data JPA)
├── service/      Regras de negócio (UsuarioService, PedidoService, FreteService...)
├── controller/   Recebem as requisições e escolhem a tela
├── config/       Login por sessão, proteção de rotas, dados iniciais
└── dto/          Objetos de formulário e carrinho
src/main/resources/templates/   Telas (View) em Thymeleaf
sql/01_schema.sql               Modelo físico para o HeidiSQL
docs/modelo-de-dados.md         Diagrama ER e dicionário de dados
```

## O que está implementado

| HU | Funcionalidade | Situação |
|---|---|---|
| 01 | Cadastro de usuário | ✅ (sem e-mail de confirmação) |
| 02 | Login com bloqueio após 5 erros (15 min) | ✅ |
| 03 | Vitrine (só produtos com estoque) | ✅ |
| 04 | Filtro por categoria | ✅ |
| 05 | Detalhe do produto + alerta de alergênicos | ✅ (sem avaliações e info. nutricional) |
| 06 | Adicionar ao carrinho (máx. 50 por item) | ✅ |
| 07 | Editar/remover itens do carrinho | ✅ |
| 08 | Frete por CEP (grátis acima de R$ 150) | ✅ |
| 09 | Finalizar pedido (cartão, PIX, boleto) | ✅ pedido fica "Aguardando pagamento"; sem gateway real e sem e-mail |
| 10 | Acompanhar pedidos | ✅ (sem e-mail) |
| 11 | Cupom de desconto (validade, limite de uso, 1 por pedido) | ✅ cupons de teste: `BEMVINDO10`, `DOCE15`, `NATAL2025` (vencido) |
| 12 | Gerenciar catálogo (admin) | ✅ foto por URL (sem upload) |
| 13 | Gerenciar estoque (admin) | ✅ baixa automática no pedido; devolução no cancelamento |
| 14 | Gerenciar pedidos (admin) | ✅ atualização automática a cada 60 s |
| 15 | Recuperar senha | ⬜ **a implementar** (depende de envio de e-mail) |

## Próximos passos previstos
- **E-mails (HU-01, 09, 10, 15):** adicionar `spring-boot-starter-mail` e um `EmailService` chamado pelos serviços.
- **Upload de imagem (HU-12 RNF#1):** `MultipartFile` limitado a JPG/PNG de 5 MB.
- **Hospedagem:** gerar o `.jar` com `mvn package` e publicar em um serviço que suporte Java e MySQL.
