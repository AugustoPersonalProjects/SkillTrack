# 🧠 SkillTrack — Sistema de Gestão de Habilidades Profissionais

![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=springboot)
![MySQL](https://img.shields.io/badge/Database-MySQL%208-blue?style=for-the-badge&logo=mysql)
![Maven](https://img.shields.io/badge/Build-Maven-blue?style=for-the-badge&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Em%20Desenvolvimento-yellowgreen?style=for-the-badge)

---

## 🚀 Visão Geral

O **SkillTrack** é uma aplicação desenvolvida em **Java Spring Boot** para o acompanhamento e gestão do desenvolvimento profissional.  
A plataforma permite que usuários cadastrem e monitorem suas **hard skills** e **soft skills**, definam **metas de aprendizado** e acompanhem seu **progresso pessoal** ao longo do tempo.

---

## 🎯 Motivação

No cenário atual de constante evolução tecnológica e profissional, muitos desenvolvem habilidades sem um meio estruturado para mensurar seu crescimento.  
O **SkillTrack** foi criado para resolver esse problema, fornecendo uma plataforma simples e completa para:

- Registrar e acompanhar **competências técnicas e comportamentais**;  
- Definir **metas e níveis de proficiência**;  
- Visualizar **progresso percentual** ao longo do tempo;  
- Obter uma **visão clara do crescimento profissional**.

---

## ⚙️ Funcionalidades

- 👤 **Gestão de Usuários**
  - Registro e autenticação via Spring Security.
  - Perfis com nome, e-mail e data de criação.

- 🧩 **Cadastro de Habilidades (Skills)**
  - Associação a categorias (ex: Programação, Design, Comunicação).
  - Definição do nível de domínio (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`).

- 🎯 **Metas (Goals)**
  - Criação de objetivos de aprendizado com prazos definidos.

- 📈 **Progresso (Progress)**
  - Registro percentual de avanço em cada habilidade.
  - Histórico para acompanhamento detalhado.

- 🗂️ **Categorias (Categories)**
  - Organização das habilidades em áreas de conhecimento.

---

## 🧩 Estrutura de Pastas

``` src/ 
├── controller/ → Controladores REST
├── service/ → Regras de negócio e validações
├── repository/ → Persistência (Spring Data JPA)
├── model/ → Entidades JPA
├── dto/ → Transferência de dados
├── mapper/ → Conversão entre DTO e Entity
└── config/ → Configurações gerais e segurança
```
---

## 🧰 Tecnologias Utilizadas

| Categoria | Tecnologias |
|------------|--------------|
| **Linguagem** | Java 21 |
| **Framework Web** | Spring Boot 3 |
| **Banco de Dados** | MySQL 8 |
| **ORM** | Spring Data JPA / Hibernate |
| **Migração de Banco** | Flyway |
| **Autenticação** | Spring Security + JWT |
| **Validação** | Jakarta Validation (Bean Validation) |
| **Documentação** | Swagger / OpenAPI |
| **Gerenciador de Dependências** | Maven |
| **Testes** | JUnit 5, Mockito |

---

## 📡 Exemplo de Uso da API

### 📍 Criar nova Skill
**POST** `/api/skills`

#### Corpo da Requisição
```json
{
  "userId": 1,
  "categoryId": 2,
  "name": "Machine Learning",
  "level": "INTERMEDIATE"
}
```
Resposta:
```json
{
  "id_skill": 10,
  "name": "Machine Learning",
  "category_id": 2,
  "level": "INTERMEDIATE",
  "progress_history": [],
  "goals": []
}
```
---

⚙️ Execução do Projeto

▶️ Rodando com Maven

```bash
mvn spring-boot:run
```
---

### 🗄️ Banco de Dados (MySQL)

O projeto usa **MySQL 8**. As migrações Flyway (`src/main/resources/db/migration`) criam todas as tabelas automaticamente na primeira execução, e o Hibernate valida (`ddl-auto=validate`) se as entidades batem com o schema.

#### Ambiente local (Docker)

```bash
docker compose up -d        # sobe o MySQL em localhost:3307
./mvnw spring-boot:run      # aplica as migrações e sobe a API em :8080
```

Para recriar o banco do zero: `docker compose down -v && docker compose up -d`.

#### Variáveis de ambiente

| Variável | Padrão (dev) | Descrição |
|----------|--------------|-----------|
| `DB_URL` | `jdbc:mysql://localhost:3307/skilltrack?...` | URL JDBC do MySQL |
| `DB_USERNAME` | `skilltrack` | Usuário do banco |
| `DB_PASSWORD` | `skilltrack` | Senha do banco |
| `JWT_SECRET` | valor de dev | **Obrigatório trocar em produção** |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` | Origens do front-end (separadas por vírgula) |

Nunca coloque credenciais reais no `application.properties`; use as variáveis acima.

> ⚠️ Não altere uma migração que já foi aplicada. Para mudar o schema, crie um novo arquivo `V2__descricao.sql`, `V3__...` etc.

---

👨‍💻 Equipe
Projeto desenvolvido por Augusto Lopes Lyra da FIAP

📅 Ano: 2025

🏁 Licença
Este projeto é de uso acadêmico e está sob a licença MIT.
Sinta-se livre para usar, estudar e evoluir o código.
