# 🧠 SkillTrack — Sistema de Gestão de Habilidades Profissionais

![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?style=for-the-badge&logo=springboot)
![Oracle](https://img.shields.io/badge/Database-Oracle%2019c-orange?style=for-the-badge&logo=oracle)
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
| **Banco de Dados** | Oracle 19c |
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

### 🗄️ Configuração do Banco de Dados Oracle

As migrações Flyway criam automaticamente as tabelas necessárias.

Edite o arquivo application.properties:

```properties
spring.datasource.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
spring.datasource.username=RMXXXXX
spring.datasource.password=XXXXXX
spring.datasource.driver-class-name=oracle.jdbc.driver.OracleDriver
spring.jpa.hibernate.ddl-auto=none
spring.flyway.enabled=true
```
---
### Exemplo de Script SQL:
```SQL
-- SKILLTRACK FULL ORACLE SCHEMA (REBUILD SAFE)
set SERVEROUTPUT on;
set VERIFY off;

DROP TABLE ST_GOALS CASCADE CONSTRAINTS;
DROP TABLE ST_PROGRESSES CASCADE CONSTRAINTS;
DROP TABLE ST_SKILLS CASCADE CONSTRAINTS;
DROP TABLE ST_CATEGORIES CASCADE CONSTRAINTS;
DROP TABLE ST_USUARIOS CASCADE CONSTRAINTS;

DROP SEQUENCE SEQ_ST_GOALS;
DROP SEQUENCE SEQ_ST_PROGRESSES;
DROP SEQUENCE SEQ_ST_SKILLS;
DROP SEQUENCE SEQ_ST_CATEGORIES;
DROP SEQUENCE SEQ_ST_USUARIOS;

-- CREATE TABLES
CREATE TABLE ST_CATEGORIES (
    ID_CATEGORY NUMBER(19) NOT NULL,
    NAME VARCHAR2(100 CHAR) NOT NULL,
    DESCRIPTION VARCHAR2(255 CHAR),
    CONSTRAINT ST_CATEGORIES_PK PRIMARY KEY (ID_CATEGORY)
)
PCTFREE 10 PCTUSED 40 TABLESPACE TBSPC_ALUNOS LOGGING
STORAGE (INITIAL 65536 NEXT 1048576 PCTINCREASE 0 MINEXTENTS 1 MAXEXTENTS 2147483645)
NO INMEMORY;

CREATE TABLE ST_USUARIOS (
    ID_USUARIO NUMBER(19) NOT NULL,
    NOME VARCHAR2(100 CHAR) NOT NULL,
    EMAIL VARCHAR2(150 CHAR) NOT NULL,
    SENHA VARCHAR2(255 CHAR) NOT NULL,
    PERFIL VARCHAR2(50 CHAR) NOT NULL,
    DATA_CRIACAO TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ST_USUARIOS_PK PRIMARY KEY (ID_USUARIO),
    CONSTRAINT ST_USUARIOS_EMAIL_UQ UNIQUE (EMAIL)
)
PCTFREE 10 PCTUSED 40 TABLESPACE TBSPC_ALUNOS LOGGING
STORAGE (INITIAL 65536 NEXT 1048576 PCTINCREASE 0 MINEXTENTS 1 MAXEXTENTS 2147483645)
NO INMEMORY;

CREATE TABLE ST_SKILLS (
    ID_SKILL NUMBER(19) NOT NULL,
    NAME VARCHAR2(100 CHAR) NOT NULL,
    DESCRIPTION VARCHAR2(255 CHAR),
    SKILL_LEVEL VARCHAR2(20 CHAR) NOT NULL,
    CATEGORY_ID NUMBER(19),
    USER_ID NUMBER(19),
    CONSTRAINT ST_SKILLS_PK PRIMARY KEY (ID_SKILL)
)
PCTFREE 10 PCTUSED 40 TABLESPACE TBSPC_ALUNOS LOGGING
STORAGE (INITIAL 65536 NEXT 1048576 PCTINCREASE 0 MINEXTENTS 1 MAXEXTENTS 2147483645)
NO INMEMORY;

CREATE TABLE ST_PROGRESSES (
    ID_PROGRESS NUMBER(19) NOT NULL,
    PERCENTAGE NUMBER(3) NOT NULL CHECK (PERCENTAGE BETWEEN 0 AND 100),
    DATE_PROGRESS TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    SKILL_ID NUMBER(19),
    CONSTRAINT ST_PROGRESSES_PK PRIMARY KEY (ID_PROGRESS)
)
PCTFREE 10 PCTUSED 40 TABLESPACE TBSPC_ALUNOS LOGGING
STORAGE (INITIAL 65536 NEXT 1048576 PCTINCREASE 0 MINEXTENTS 1 MAXEXTENTS 2147483645)
NO INMEMORY;

CREATE TABLE ST_GOALS (
    ID_GOAL NUMBER(19) NOT NULL,
    DESCRIPTION VARCHAR2(255 CHAR) NOT NULL,
    DEADLINE TIMESTAMP,
    SKILL_ID NUMBER(19),
    CONSTRAINT ST_GOALS_PK PRIMARY KEY (ID_GOAL)
)
PCTFREE 10 PCTUSED 40 TABLESPACE TBSPC_ALUNOS LOGGING
STORAGE (INITIAL 65536 NEXT 1048576 PCTINCREASE 0 MINEXTENTS 1 MAXEXTENTS 2147483645)
NO INMEMORY;

ALTER TABLE ST_SKILLS
ADD CONSTRAINT ST_SKILLS_CATEGORY_FK FOREIGN KEY (CATEGORY_ID) REFERENCES ST_CATEGORIES (ID_CATEGORY);

ALTER TABLE ST_SKILLS
ADD CONSTRAINT ST_SKILLS_USER_FK FOREIGN KEY (USER_ID) REFERENCES ST_USUARIOS (ID_USUARIO);

ALTER TABLE ST_PROGRESSES
ADD CONSTRAINT ST_PROGRESSES_SKILL_FK FOREIGN KEY (SKILL_ID) REFERENCES ST_SKILLS (ID_SKILL);

ALTER TABLE ST_GOALS
ADD CONSTRAINT ST_GOALS_SKILL_FK FOREIGN KEY (SKILL_ID) REFERENCES ST_SKILLS (ID_SKILL);

CREATE SEQUENCE SEQ_ST_CATEGORIES START WITH 1 INCREMENT BY 1 CACHE 20;
CREATE SEQUENCE SEQ_ST_USUARIOS START WITH 1 INCREMENT BY 1 CACHE 20;
CREATE SEQUENCE SEQ_ST_SKILLS START WITH 1 INCREMENT BY 1 CACHE 20;
CREATE SEQUENCE SEQ_ST_PROGRESSES START WITH 1 INCREMENT BY 1 CACHE 20;
CREATE SEQUENCE SEQ_ST_GOALS START WITH 1 INCREMENT BY 1 CACHE 20;

SELECT * FROM st_goals;
SELECT * FROM st_skills;
SELECT * FROM st_progresses;
SELECT * FROM st_category;
SELECT * FROM st_usuarios;
```
---
### OBS sobre o banco de dados:
---
Durante a execução do projeto, o Flyway (ferramenta responsável por gerenciar as migrações do banco de dados) cria automaticamente uma tabela chamada:

```ngix
FLYWAY_SCHEMA_HISTORY
```

- Essa tabela serve para registrar o histórico de migrações aplicadas, armazenando informações como o número da versão, descrição do script, status da execução e data da aplicação.
Ou seja, ela é essencial para o Flyway saber quais scripts SQL já foram executados e quais ainda precisam ser aplicados.

- No entanto, em alguns casos especialmente em bancos Oracle compartilhados (como o da FIAP) — pode ser necessário excluir essa tabela manualmente antes de rodar o projeto novamente, pois:
O Flyway impede reexecução de scripts já aplicados para evitar inconsistências;
Se o schema for compartilhado entre diferentes projetos, o histórico pode conflitar;
Alterações nos scripts (por exemplo, renomear ou modificar um arquivo SQL já executado) podem gerar erros de validação.
Por isso, ao reconstruir o banco ou reiniciar o projeto do zero, é seguro excluir a tabela FLYWAY_SCHEMA_HISTORY, permitindo que o Flyway recrie todo o esquema limpo e reaplique as migrações na ordem correta.
---
🧩 Em resumo:<br>
---
Exclua a tabela FLYWAY_SCHEMA_HISTORY apenas quando quiser reexecutar todas as migrações do zero ou quando houver conflitos de versionamento no histórico do Flyway.
---

👨‍💻 Equipe
Projeto desenvolvido por Augusto Lopes Lyra da FIAP

📅 Ano: 2025

🏁 Licença
Este projeto é de uso acadêmico e está sob a licença MIT.
Sinta-se livre para usar, estudar e evoluir o código.
