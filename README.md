# 🧠 SkillTrack — Sistema de Gestão de Habilidades Profissionais

[![CI](https://github.com/AugustoPersonalProjects/SkillTrack/actions/workflows/ci.yml/badge.svg)](https://github.com/AugustoPersonalProjects/SkillTrack/actions/workflows/ci.yml)
[![CodeQL](https://github.com/AugustoPersonalProjects/SkillTrack/actions/workflows/codeql.yml/badge.svg)](https://github.com/AugustoPersonalProjects/SkillTrack/actions/workflows/codeql.yml)

![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-brightgreen?style=for-the-badge&logo=springboot)
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

- 🛡️ **Administração (perfil ADMIN)**
  - Resumo do sistema em `GET /admin/resumo`: totais e contagem de skills e metas por usuário.
  - Gestão de usuários (perfil, dados e exclusão) e de categorias.
  - O sistema nunca fica sem administrador: o último ADMIN não pode ser rebaixado nem excluído.

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
| **Testes** | JUnit 5, MockMvc, Testcontainers (MySQL) |

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

O `spring-boot:run` ativa o perfil `dev`, que define um `JWT_SECRET` local. Ao iniciar a aplicação pela IDE, ative esse perfil na configuração de execução.

#### Variáveis de ambiente

| Variável | Padrão (dev) | Descrição |
|----------|--------------|-----------|
| `DB_URL` | `jdbc:mysql://localhost:3307/skilltrack?...` | URL JDBC do MySQL |
| `DB_USERNAME` | `skilltrack` | Usuário do banco |
| `DB_PASSWORD` | `skilltrack` | Senha do banco |
| `JWT_SECRET` | só no perfil `dev` | **Obrigatória em produção**: sem ela a aplicação não inicia. Use um valor longo e aleatório |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:*,http://127.0.0.1:*` | Origens do front-end (separadas por vírgula) |
| `ADMIN_EMAIL` | vazio | E-mail do primeiro ADMIN. Só é usado enquanto o sistema não tem nenhum ADMIN |
| `ADMIN_PASSWORD` | vazio | Senha do primeiro ADMIN (mínimo de 6 caracteres). Se a conta já existir, precisa ser a senha dela |
| `ADMIN_NAME` | `Administrador` | Nome do ADMIN inicial |

Nunca coloque credenciais reais no `application.properties`; use as variáveis acima.

> ⚠️ Não altere uma migração que já foi aplicada. Para mudar o schema, crie um novo arquivo `V2__descricao.sql`, `V3__...` etc.

### 🧪 Testes

```bash
./mvnw test
```

Os testes sobem a API completa, com as migrações e a configuração de produção, em um MySQL real criado pelo [Testcontainers](https://testcontainers.com). Por isso o Docker precisa estar ligado, mas não é necessário rodar o `docker compose`.

A cada push em `main` e em cada pull request, o GitHub Actions roda os testes, constrói a imagem Docker e faz a análise de segurança com o CodeQL. O Dependabot propõe as atualizações de dependências.


### ☁️ Deploy (Render + Aiven + Vercel)

| Camada | Plataforma | URL |
|--------|------------|-----|
| Front-end | Vercel | https://skill-track-front-end.vercel.app |
| API (Java) | Render | https://skilltrack-qohl.onrender.com |
| Banco de dados | Aiven (MySQL) | acesso restrito (credenciais no console do Aiven) |

#### 1. Banco de dados (Aiven)

1. Crie um serviço **MySQL** no Aiven.
2. Na aba **Overview**, copie Host, Port, User e Password. O banco padrão é `defaultdb`.
3. O Aiven exige SSL. Por isso a URL JDBC usa `sslMode=REQUIRED`, e não `useSSL=false` como no ambiente local.

O Aiven mostra a conexão no formato `mysql://usuario:senha@host:porta/defaultdb?ssl-mode=REQUIRED`. A aplicação espera a URL JDBC, com usuário e senha em variáveis separadas:

```properties
DB_URL=jdbc:mysql://mysql-skilltrack-skilltrack.j.aivencloud.com:26192/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
```

#### 2. API (Render)

O projeto possui um `Dockerfile`, usado pelo Render para o build.

1. Crie um **Web Service** a partir deste repositório do GitHub, com runtime **Docker**.
2. Em **Environment**, configure as variáveis:

```properties
DB_URL=jdbc:mysql://mysql-skilltrack-skilltrack.j.aivencloud.com:26192/defaultdb?sslMode=REQUIRED&serverTimezone=UTC
DB_USERNAME=avnadmin
DB_PASSWORD=<senha do Aiven>
JWT_SECRET=<valor longo e aleatório>
CORS_ALLOWED_ORIGINS=https://skill-track-front-end.vercel.app
ADMIN_EMAIL=<seu e-mail>
ADMIN_PASSWORD=<senha forte; se a conta já existir, a senha dela>
```

A porta é definida pelo Render na variável `PORT`, que a aplicação já utiliza. As migrações do Flyway criam as tabelas e as categorias padrão na primeira inicialização.

Para criar o primeiro acesso ao painel de administração, defina `ADMIN_EMAIL` e `ADMIN_PASSWORD`. Enquanto o sistema não tiver nenhum ADMIN, a aplicação cria essa conta ao iniciar. Se o e-mail já estiver cadastrado, a conta só é promovida quando `ADMIN_PASSWORD` for a senha dela, porque o cadastro público não confirma quem é o dono do e-mail. Depois disso, os perfis são geridos pelo painel. É preciso sair e entrar de novo no front para o menu de administração aparecer.

> ℹ️ No plano gratuito, o Render suspende o serviço após ~15 minutos sem uso, e a primeira requisição seguinte pode levar até um minuto.

#### 3. Front-end (Vercel)

O front-end é publicado no Vercel e consome a API em `https://skilltrack-qohl.onrender.com`. O domínio do Vercel precisa estar em `CORS_ALLOWED_ORIGINS` na API; caso contrário, o navegador bloqueia as requisições.
---

👨‍💻 Equipe
Projeto desenvolvido por Augusto Lopes Lyra da FIAP

🤖 Este projeto foi desenvolvido com auxílio de Inteligência Artificial no deploy, no versionamento e em partes do código (interface do front-end, área de administração e cálculo do dashboard).

📅 Ano: 2025

🏁 Licença
Este projeto é de uso acadêmico e está sob a licença MIT.
Sinta-se livre para usar, estudar e evoluir o código.
