# 🍂 Autumn Framework

O **Autumn** é um framework inspirado no **Spring Boot**, criado para estudo e entendimento dos conceitos de injeção de dependência, mapeamento de rotas, configuração de banco de dados e gerenciamento de aplicações Java baseadas em anotações.  

---

## 🚀 Estrutura Geral

- **AutumnApplication**  
  Responsável por inicializar a aplicação, realizar o *scan* das classes do projeto e configurar o servidor embutido.  
  - Identifica e instancia os componentes anotados.  
  - Realiza injeção de dependências.  
  - Sobe o servidor HTTP e registra o `AutumnServlet`.

- **AutumnServlet**  
  Servlet que recebe as requisições HTTP e as encaminha para os *controllers* definidos na aplicação.

- **Context**  
  Responsável por:  
  - Fazer o *scan* do pacote da aplicação.  
  - Instanciar os beans.  
  - Injetar dependências.  
  - Armazenar todas as instâncias em um mapa centralizado para reuso.  

---

## 📦 Anotações Disponíveis

O Autumn implementa diversas anotações semelhantes às do Spring:

- **@Bean** – Cria instâncias gerenciadas pelo framework.  
- **@Component** – Marca uma classe para ser instanciada automaticamente.  
- **@Configuration** – Define classes de configuração (ex.: banco de dados).  
- **@Controller** – Define uma classe controladora para tratar requisições.  
- **@ControllerAdvice** – Centraliza tratamento de exceções.  
- **@DeleteMapping / @GetMapping / @PostMapping / @PutMapping** – Mapeiam métodos para rotas HTTP específicas.  
- **@ExceptionHandler** – Define métodos para lidar com exceções lançadas nos controllers.  
- **@Inject** – Injeção de dependências, equivalente ao `@Autowired` do Spring.  
- **@PathVariable** – Mapeia parâmetros de rota da URL para variáveis do método.  
- **@RequestBody** – Faz o *binding* do corpo da requisição para um objeto.  
- **@Repository** – Identifica classes de persistência.  
- **@RequestPath** – Define o caminho base para um controller.  

---

## ⚙️ Configuração de Banco de Dados

O Autumn oferece suporte a diferentes bancos relacionais e também ao MongoDB, configurados através do arquivo `application.properties`.  

### Exemplos de chaves de configuração:

- **Seleção do banco**  
  ```properties
  autumn.datasource.type=h2 | postgres | mysql | mongodb
  ```

- **Configurações JPA (para bancos relacionais)**  
  - `autumn.jpa.hibernate.hbm2ddl` → Estratégia de geração de schema (ex.: update, create).  
  - `autumn.jpa.hibernate.dialect` → Dialeto do Hibernate.  
  - `autumn.jpa.show_sql` → Exibe as queries no log.  

- **Configuração por banco**  
  - **H2**: driver, url, usuário e senha.  
  - **PostgreSQL**: driver, url, usuário e senha.  
  - **MySQL**: driver, url, usuário e senha.  
  - **MongoDB**: URI e database.  

### Entity Manager

O framework constrói automaticamente o `EntityManagerFactory` a partir das propriedades carregadas, registrando as entidades mapeadas no contexto.  

Para ativar o `EntityManager`, basta criar uma configuração com `@Bean` em uma classe anotada com `@Configuration`.

---

## 🖥️ Inicialização da Aplicação

Um projeto Autumn precisa apenas de uma classe principal com o método `main`, chamando:

```java
AutumnApplication.run(App.class);
```

Esse processo:  
1. Executa o *scan* de classes no pacote informado.  
2. Cria e injeta instâncias no contexto.  
3. Configura o banco de dados conforme definido no `application.properties`.  
4. Sobe o servidor embutido e registra o servlet.  

---

## 🔒 Tratamento de Exceções

- **@ControllerAdvice** → Centraliza o tratamento de erros da aplicação.  
- **@ExceptionHandler** → Define métodos específicos para capturar e processar exceções.  

---

## 📂 Estrutura de Projeto Sugerida

Um projeto Autumn pode ser organizado da seguinte forma:

```
src/main/java/com/seuprojeto
│
├── App.java                      # Classe principal que inicia a aplicação
│
├── config
│   └── DataBaseConfig.java       # Configurações de banco de dados, beans etc.
│
├── controller
│   ├── UserController.java       # Exemplo de controller REST
│   └── ErrorHandler.java         # Tratamento centralizado de exceções
│
├── entity
│   └── User.java                 # Entidades JPA (mapeadas para o banco de dados)
│
├── repository
│   └── UserRepository.java       # Classes de acesso a dados
│
├── service
│   └── UserService.java          # Contém a lógica de negócio
│
└── util
    └── MapperUtils.java          # Classes utilitárias (opcional)
```

---

## 📄 Arquivo de configuração

Na pasta `resources`, deve existir um `application.properties`, onde ficam as configurações:

```
src/main/resources/application.properties
```

Exemplo de chaves:  

```properties
autumn.datasource.type=postgres
autumn.datasource.postgres.url=jdbc:postgresql://localhost:5432/meubanco
autumn.datasource.postgres.user=postgres
autumn.datasource.postgres.password=postgres

autumn.jpa.hibernate.hbm2ddl=update
autumn.jpa.show_sql=true
```

---

## 🔄 Fluxo típico de uma requisição

1. O cliente faz uma requisição HTTP → `AutumnServlet` intercepta.  
2. O **Controller** correspondente é chamado (ex.: `UserController`).  
3. O Controller aciona um **Service** para processar a lógica de negócio.  
4. O Service acessa os dados através de um **Repository**.  
5. O Repository utiliza o **EntityManager** configurado pelo Autumn.  
6. A resposta é retornada ao cliente.  

### 📊 Fluxograma

```text
Cliente (HTTP Request)
        │
        ▼
   AutumnServlet
        │
        ▼
   Controller (@Controller)
        │
        ▼
   Service (@Component / lógica)
        │
        ▼
   Repository (@Repository)
        │
        ▼
  EntityManager (JPA)
        │
        ▼
   Banco de Dados
        │
        ▼
Cliente (HTTP Response)
```

---

## 📖 Exemplo Prático: CRUD de Usuários

### 1. Entidade

```java
@Entity
public class User {
    private Long id;
    private String name;
    private String email;
}
```

### 2. Repositório

```java
@Repository
public class UserRepository {
    // Métodos para salvar, buscar e remover usuários
}
```

### 3. Serviço

```java
@Component
public class UserService {

    @Inject
    private UserRepository userRepository;

    public List<User> findAll() { ... }
    public User findById(Long id) { ... }
    public User save(User user) { ... }
    public void delete(Long id) { ... }
}
```

### 4. Controller

```java
@Controller
@RequestPath("/users")
public class UserController {

    @Inject
    private UserService userService;

    @GetMapping
    public List<User> listAll() { ... }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) { ... }

    @PostMapping
    public User create(@RequestBody User user) { ... }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestBody User user) { ... }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { ... }
}
```

### 5. Tratamento de Exceções

```java
@ControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(NotFoundException.class)
    public MessageError handleIllegalArgument(NotFoundException ex) {
        return new MessageError("", HttpStatus.NOT_FOUND_404, ex.getMessage(), "");
    }
}
```

### 6. Classe Principal

```java
public class App {
    public static void main(String[] args) {
        AutumnApplication.run(App.class);
    }
}
```

✅ Endpoints expostos:  

- `GET /users` → Lista todos os usuários  
- `GET /users/{id}` → Busca usuário por ID  
- `POST /users` → Cria um novo usuário  
- `PUT /users/{id}` → Atualiza um usuário existente  
- `DELETE /users/{id}` → Remove um usuário  

---

## 🌱 Resumo dos Principais Recursos

✔️ Injeção de dependências com `@Inject`  
✔️ Criação de beans e componentes gerenciados  
✔️ Configuração de banco de dados relacional (H2, PostgreSQL, MySQL) e NoSQL (MongoDB)  
✔️ EntityManager configurado via propriedades  
✔️ Servidor embutido para receber requisições HTTP  
✔️ Controle de rotas via anotações (@GetMapping, @PostMapping, etc.)  
✔️ Tratamento centralizado de exceções  

---

👉 O Autumn é uma base sólida para compreender como funcionam frameworks complexos como o Spring Boot, permitindo explorar desde o ciclo de vida dos beans até a integração com banco de dados e controle de rotas HTTP.  
