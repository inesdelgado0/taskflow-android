# TaskFlow – Gestão de Projetos e Tarefas

> Aplicação móvel Android para gestão de projetos e tarefas em equipa — Computação Móvel 2025/2026 | ESTG/IPVC

---

## Índice

1. [Descrição do Projeto](#descrição-do-projeto)
2. [Funcionalidades](#funcionalidades)
3. [Requisitos Funcionais](#requisitos-funcionais)
4. [Requisitos Não Funcionais](#requisitos-não-funcionais)
5. [Arquitetura](#arquitetura)
6. [Tecnologias e Dependências](#tecnologias-e-dependências)
7. [Estrutura do Projeto](#estrutura-do-projeto)
8. [Configuração e Instalação](#configuração-e-instalação)
9. [Como Usar a Aplicação](#como-usar-a-aplicação)
10. [API Reference](#api-reference)
11. [Base de Dados Local](#base-de-dados-local)
12. [Base de Dados Remota](#base-de-dados-remota)
13. [Sincronização Offline](#sincronização-offline)
14. [Internacionalização (i18n)](#internacionalização-i18n)
15. [Testes](#testes)
16. [Geração do APK](#geração-do-apk)
17. [Gestão do Projeto (Trello)](#gestão-do-projeto-trello)
18. [Equipa](#equipa)

---

## Descrição do Projeto

**TaskFlow** é uma aplicação móvel Android desenvolvida em **Kotlin** no âmbito da unidade curricular de Computação Móvel (Tema 4 — Gestão de Projetos). O objetivo é criar uma plataforma completa de **gestão de projetos e tarefas** para equipas, com suporte a múltiplos perfis por utilizador, sincronização offline/online, visualização de estatísticas e notificações.

### Visão Geral

A aplicação assenta num modelo **offline-first**: os dados são guardados localmente via **Room (SQLite)** e sincronizados com uma **REST API Node/Express** que persiste informação em **Supabase/PostgreSQL**. A lógica de negócio segue a arquitetura **MVVM com Clean Architecture**, garantindo separação de responsabilidades, testabilidade e escalabilidade. A interface é construída inteiramente com **Jetpack Compose**.

### Perfis de Utilizador

Um utilizador pode ter uma ou mais roles em simultâneo. No Android existe uma role ativa/principal para navegação imediata, mas a API e a base de dados suportam múltiplas roles via `roles` + `user_roles`.

| Perfil | Responsabilidades principais |
|---|---|
| **Administrador** | Gerir utilizadores, projetos e exportar estatísticas globais |
| **Gestor de Projeto** | Gerir tarefas, associar utilizadores e avaliar performance |
| **Utilizador** | Executar tarefas, registar progresso e adicionar observações |

---

## Funcionalidades

### Onboarding e Acesso
- Intro sliders (apresentação da app no primeiro arranque)
- Ecrã de boas-vindas com opção de Login ou Registo
- Criar conta (nome, username, email, password, fotografia)
- Iniciar sessão com JWT (refresh automático via `TokenRefreshInterceptor`)
- Logout

### Gestão de Perfil (todos os perfis)
- Consultar e editar dados pessoais: nome, username, email, fotografia e palavra-passe

### Administrador
- CRUD completo de projetos (nome, descrição, datas, gestor responsável)
- CRUD de contas de utilizadores e gestores de projeto
- Associar gestor de projeto a cada projeto
- Exportar estatísticas em **CSV** (por utilizador / projeto / tarefa)
- Pesquisar e filtrar projetos, tarefas e utilizadores

### Gestor de Projeto
- CRUD de tarefas (título, descrição, prioridade, prazo, estado)
- Associar utilizadores a projetos e tarefas (relação N:M)
- Visualizar tarefas concluídas e por concluir
- Marcar projeto como concluído
- Avaliar performance da equipa (classificação 1–5 + comentário opcional)
- Exportar estatísticas em **CSV**

### Utilizador
- Consultar tarefas atribuídas
- Registar data, local, percentagem de conclusão e tempo dispensado
- Marcar tarefa como concluída (a 100% é automático)
- Adicionar observações (texto e/ou fotografias via câmara/galeria)
- Visualizar lista de tarefas pendentes e histórico de concluídas

### Funcionalidades Transversais
- **Offline-first**: operações guardadas na `sync_queue` local e sincronizadas via WorkManager
- **Notificações**: canais separados para tarefas e prazos; lembretes periódicos via `DeadlineReminderWorker`
- **Pesquisa e filtragem**: por nome, estado, prioridade, datas ou utilizador
- **Validação**: mensagens claras em falhas de rede, autenticação inválida ou dados incorretos
- **Internacionalização**: Português (PT) e Inglês (EN)
- **Responsividade**: portrait forçado
- **Auditoria**: registo de autenticações, CRUD e sincronizações em `audit_log`
- **Token Refresh**: renovação automática do JWT sem interrupção da sessão

---

## Requisitos Funcionais

| ID | Descrição | Estado |
|---|---|---|
| **RF01** | Onboarding, criar conta, login, logout | ✅ Implementado |
| **RF02** | Gestão de perfil (nome, username, email, foto, password) | ✅ Implementado |
| **RF03** | Três perfis: Administrador, Gestor, Utilizador | ✅ Implementado |
| **RF04** | Admin: CRUD de projetos + associar gestor | ✅ Implementado |
| **RF05** | Admin: CRUD de utilizadores e gestores | ✅ Implementado |
| **RF06** | Gestor: CRUD de tarefas (prioridade, prazo, estado) | ✅ Implementado |
| **RF07** | Associação N:M utilizadores ↔ projetos e tarefas | ✅ Implementado |
| **RF08** | Ver progresso, marcar projeto concluído, avaliar utilizadores | ✅ Implementado |
| **RF09** | Utilizador: registar data, local, % conclusão, tempo | ✅ Implementado |
| **RF10** | Observações com suporte a fotografias | ✅ Implementado |
| **RF11** | Histórico de concluídas e lista de pendentes | ✅ Implementado |
| **RF12** | Exportação de estatísticas em PDF/CSV |  ✅ Implementado |
| **RF13** | Sincronização automática offline → online | ✅ Implementado |
| **RF14** | Pesquisa e filtragem avançada | ✅ Implementado |
| **RF15** | Notificações (tarefas, prazos, sync) | ✅ Implementado |
| **RF16** | Validação e mensagens de erro claras | ✅ Implementado |

---

## Requisitos Não Funcionais

| ID | Descrição | Estado |
|---|---|---|
| **RNF01** | Plataforma Android, linguagem Kotlin | ✅ |
| **RNF02** | MVVM + Clean Architecture | ✅ |
| **RNF03** | Persistência local com Room/SQLite | ✅ |
| **RNF04** | REST API HTTPS + autenticação JWT | ✅ |
| **RNF05** | Funcionamento offline com sync posterior | ✅ |
| **RNF06** | Tempo de resposta < 2 segundos | ✅ |
| **RNF07** | Estabilidade e minimização de falhas | ✅ |
| **RNF08** | Suporte a Português e Inglês | ✅ |
| **RNF09** | Interface adaptada a portrait | ✅ |
| **RNF10** | Registo interno de ações (auditoria) | ✅ |
| **RNF11** | Interface intuitiva e consistente | ✅ |
| **RNF12** | Suporte a testes unitários e instrumentados | ✅ |

---

## Arquitetura

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                      │
│         (Composables, Screens, Navigation)      │
│         Observa StateFlow / collectAsState()    │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│               ViewModel Layer                   │
│     (Lógica de apresentação, state holders)     │
│         Usa UseCases / Repositórios             │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│               Domain Layer                      │
│     (Use Cases, Entities, Repository Interfaces)│
│           Independente de frameworks            │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼───────────────────────────┐
│               Data Layer                         │
│  ┌─────────────────┐   ┌───────────────────────┐ │
│  │  Local (Room)   │   │  Remote (Retrofit API)│ │
│  │  SQLite Cache   │   │  REST + JWT Auth      │ │
│  └────────┬────────┘   └──────────┬────────────┘ │
│           └──────────┬────────────┘              │
│              Repository Impl                     │
└──────────────────────────────────────────────────┘
```

### Fluxo de Dados

```
User Action → ViewModel → UseCase → Repository
                                       ├─ Room (local cache)
                                       └─ Retrofit (API remota)
                                              ↓
                                    UiState (StateFlow) → Composable
```

---

## Tecnologias e Dependências

### Android

| Biblioteca | Versão | Uso |
|---|---|---|
| Kotlin | 2.2.x | Linguagem principal |
| Jetpack Compose BOM | 2024.x | UI declarativa |
| Material Design 3 | via BOM | Componentes UI |
| Compose Navigation | 2.8.x | Navegação entre ecrãs |
| Room | 2.6.1 | Persistência local (SQLite) |
| DataStore Preferences | 1.1.1 | JWT + preferências |
| Hilt | 2.56.2 | Injeção de dependências |
| Kotlin Coroutines + Flow | 1.7.3 | Programação assíncrona |
| Retrofit + OkHttp | 2.11.0 / 4.12.0 | HTTP REST client |
| Gson Converter | 2.11.0 | Serialização JSON |
| WorkManager | 2.9.0 | Background sync + alarmes |
| Coil Compose | 2.7.0 | Carregamento de imagens |

### Backend

| Tecnologia | Versão | Uso |
|---|---|---|
| Node.js + Express | 4.x | REST API |
| Supabase / PostgreSQL | 2.x | Base de dados remota |
| jsonwebtoken | 9.x | Autenticação JWT |
| bcryptjs | — | Hash de passwords |
| Render | — | Deploy gratuito |

---

## Estrutura do Projeto

```
TaskFlow/
├── app/src/main/java/com/taskflow/app/
│   ├── audit/                    # AuditLogger.kt
│   ├── data/
│   │   ├── export/               # StatisticsCsvFormatter, StatisticsFileExporter
│   │   ├── local/
│   │   │   ├── converter/        # TypeConverters Room
│   │   │   ├── dao/              # 9 DAOs (User, Project, Task, UserTask, ...)
│   │   │   ├── entity/           # 11 entidades Room
│   │   │   └── database/         # AppDatabase.kt (versão 2, exportSchema=true)
│   │   ├── remote/
│   │   │   ├── api/              # AuthApi, ProjectApi, TaskApi, UserApi, ...
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── interceptor/      # AuthInterceptor, NetworkErrorInterceptor, TokenRefreshInterceptor
│   │   │   └── TokenManager.kt
│   │   └── repository/           # 8 RepositoryImpl
│   ├── di/                       # DatabaseModule, NetworkModule, RepositoryModule
│   ├── domain/
│   │   ├── model/                # User, Project, Task, Observation, Statistics, ...
│   │   ├── repository/           # Interfaces dos repositórios
│   │   ├── usecase/              # UseCases por perfil (admin, manager, user, auth, sync, statistics)
│   │   └── util/                 # Enums.kt (UserRole, TaskStatus, TaskPriority, ...)
│   ├── notification/             # TaskFlowNotifier, DeadlineReminderWorker, TaskNotificationScheduler
│   ├── sync/                     # SyncManager.kt, SyncWorker.kt
│   ├── ui/
│   │   ├── admin/                # 6 screens + ViewModel Admin
│   │   ├── auth/                 # LoginScreen, RegisterScreen, AuthViewModel
│   │   ├── common/               # SearchScreen, SearchViewModel, componentes reutilizáveis
│   │   │   └── components/       # Avatars, Buttons, Cards, Fields, Layout, StatusIndicators
│   │   ├── manager/              # 11 screens + ViewModels Gestor
│   │   ├── navigation/           # NavGraph.kt, Routes.kt
│   │   ├── onboarding/           # OnboardingScreen.kt
│   │   ├── profile/              # ProfileScreen, ProfileViewModel
│   │   ├── project/              # ProjectFormScreen
│   │   ├── theme/                # Theme.kt, Type.kt, Colors.kt
│   │   └── user/                 # 6 screens + ViewModels Utilizador
│   └── util/                     # ApiResult, safeApiCall, NetworkError, ConnectivityObserver
├── backend/
│   ├── src/
│   │   ├── config/               # supabase.js
│   │   ├── middleware/           # auth.js (JWT verification)
│   │   ├── routes/               # auth, projects, tasks, users, observations,
│   │   │                         # evaluations, stats, audit, syncQueue, devices
│   │   └── server.js
│   └── supabase/
│       ├── schema.sql            # DDL completo PostgreSQL
│       └── seed.sql              # Dados de teste
└── README.md
```

---

## Configuração e Instalação

### Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 21+ |
| Android SDK | API 26 (target/compile API 36) |
| Git | 2.x |
| Node.js | 20+ (backend local) |
| Conta Supabase | Para base de dados remota |

### 1. Clonar o Repositório

```bash
git clone https://github.com/inesdelgado0/taskflow-android.git
cd taskflow-android
```

### 2. Configurar o Backend Local

```bash
cd backend
npm install
cp .env.example .env        # Linux/macOS
# copy .env.example .env    # Windows
```

Preencher `backend/.env`:

```env
PORT=3000
SUPABASE_URL=https://teu-project-ref.supabase.co
SUPABASE_SERVICE_ROLE_KEY=service_role_key_do_supabase
JWT_SECRET=uma_chave_longa_aleatoria
JWT_EXPIRES_IN=7d
```

No Supabase, correr `backend/supabase/schema.sql` no SQL Editor. Iniciar a API:

```bash
npm run dev
# Verificar: http://localhost:3000/health → {"status":"ok"}
```

### 3. Configurar Android

A app aponta por padrão para a API de produção (`https://taskflow-api-fvln.onrender.com/v1/`).  
Para desenvolvimento local, editar `NetworkModule.kt`:

```kotlin
private const val BASE_URL = "http://10.0.2.2:3000/v1/"  // emulador → localhost
```

### 4. Build e Execução

```bash
# Sincronizar dependências
./gradlew build

# Instalar debug no dispositivo/emulador
./gradlew installDebug
```

Ou abrir no Android Studio e clicar em **Run ▶**.

### 5. Instalar via APK

1. Ativar **Instalar apps desconhecidas** nas definições do Android
2. Copiar `app-debug.apk` para o dispositivo
3. Abrir o ficheiro e seguir as instruções

---

## Como Usar a Aplicação

### Primeiro Arranque

1. **Onboarding** — sliders introdutórios (apenas na primeira execução)
2. **Registo** — criar conta com nome, username, e-mail e password
3. **Login** — autenticação JWT guardada via DataStore

### Perfil: Administrador

| Ação | Como fazer |
|---|---|
| Criar projeto | Dashboard → Novo Projeto → preencher dados + atribuir gestor |
| Gerir utilizadores | Menu Utilizadores → Novo / Editar / Eliminar |
| Exportar estatísticas | Dashboard → Estatísticas → CSV |

### Perfil: Gestor de Projeto

| Ação | Como fazer |
|---|---|
| Criar tarefa | Projeto → Tarefas → Nova Tarefa |
| Associar utilizadores | Tarefa → Adicionar Membro |
| Concluir projeto + avaliar | Projeto → Concluir → avaliar cada membro (1–5) |

### Perfil: Utilizador

| Ação | Como fazer |
|---|---|
| Registar progresso | As Minhas Tarefas → selecionar → preencher data/local/% |
| Adicionar observação | Tarefa → Observações → Nova (texto e/ou foto) |
| Ver histórico | Separador Histórico |

---

## API Reference

**Base URL produção:** `https://taskflow-api-fvln.onrender.com/v1/`  
**Base URL local:** `http://10.0.2.2:3000/v1/` (emulador)

Todos os endpoints (exceto `/auth/*`) requerem `Authorization: Bearer <token>`.

### Autenticação

```http
POST /auth/login      → { token, user }
POST /auth/register   → 201 Created
POST /auth/refresh    → { token }
POST /auth/logout     → 200 OK
```

### Projetos

```http
GET    /projects
POST   /projects
GET    /projects/{id}
PUT    /projects/{id}
DELETE /projects/{id}
PUT    /projects/{id}/complete
PUT    /projects/{id}/manager
```

### Tarefas

```http
GET    /projects/{id}/tasks
POST   /projects/{id}/tasks
GET    /tasks/{id}
PUT    /tasks/{id}
DELETE /tasks/{id}
PUT    /tasks/{id}/status
```

### Utilizadores, Observações, Avaliações, Estatísticas

```http
GET/POST/PUT/DELETE /users/{id}
GET/POST/DELETE     /tasks/{id}/observations
POST                /users/{id}/evaluate
GET                 /stats/users/{id}
GET                 /stats/projects/{id}
GET                 /stats/export?format=csv
```

---

## Base de Dados Local

Room SQLite, versão 2 do schema (`exportSchema = true`).

| Tabela | Descrição |
|---|---|
| `users` | Dados dos utilizadores |
| `roles` | Perfis (ADMIN, MANAGER, USER) |
| `user_roles` | N:M utilizador–perfil |
| `projects` | Projetos |
| `tasks` | Tarefas com prioridade, prazo e estado |
| `user_project` | N:M utilizador–projeto |
| `user_task` | N:M utilizador–tarefa + progresso de execução |
| `observations` | Observações com foto opcional |
| `evaluations` | Avaliações de desempenho por projeto |
| `audit_log` | Registo append-only de ações |
| `sync_queue` | Fila FIFO de operações offline pendentes |

---

## Base de Dados Remota

Supabase/PostgreSQL. Schema completo em `backend/supabase/schema.sql`.


## Sincronização Offline

```
Utilizador executa ação (sem internet)
        ↓
Operação guardada em Room (sync_queue)
        ↓
ConnectivityManager deteta ligação restabelecida
        ↓
WorkManager dispara SyncWorker
        ↓
SyncWorker processa fila FIFO
    ├─ Sucesso → remove da fila
    └─ Falha   → retry com backoff exponencial (máx. 3 tentativas)
        ↓
Notificação ao utilizador (sync concluída / falhada)
```
Worker periódico a cada **15 minutos** enquanto há conectividade.

---

## Internacionalização (i18n)

| Idioma | Código | Ficheiro |
|---|---|---|
| Português | `pt` (padrão) | `res/values/strings.xml` |
| Inglês | `en` | `res/values-en/strings.xml` |

Seleção automática pelo dispositivo; pode ser alterada manualmente nas definições, persistindo via DataStore.

---

## Testes

### Testes Unitários

```bash
./gradlew test
```

Cobertura:
- `UpdateTaskProgressUseCaseTest` — 4 cenários (progresso, localização nula, conclusão, inputs inválidos)
- `UserTaskListUseCasesTest` — tarefas pendentes e concluídas
- `StatisticsUseCasesTest` — cálculo de taxas e totais
- `StatisticsCsvFormatterTest` — estrutura e escape CSV

### Testes Instrumentados

```bash
./gradlew connectedAndroidTest
```

Requer emulador ou dispositivo. Cobre:
- `LoginFormContentTest` — submissão e exibição de erros
- `ProjectFormContentTest` — criação de projeto
- `TaskFormContentTest` — criação de tarefa
- `UserHistoryContentTest` — histórico de tarefas

---

## Geração do APK

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

```bash
# 1. Gerar keystore (uma vez)
keytool -genkey -v -keystore taskflow.keystore \
  -alias taskflow -keyalg RSA -keysize 2048 -validity 10000

# 2. Configurar build.gradle.kts com signingConfigs

# 3. Gerar APK
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## Gestão do Projeto (Trello)

[https://trello.com/b/taskflow-android](https://trello.com/b/taskflow-android)

| Coluna | Descrição |
|---|---|
| **Semana X** | Tarefas planeadas para a semana |
| **A Fazer** | Trabalho em curso |
| **Review** | Em revisão |
| **Feito** | Concluído |

---

## Uso de Inteligência Artificial

Declaramos explicitamente o uso das seguintes ferramentas de IA:

- **Claude (Anthropic)** — geração e revisão de código Kotlin/Compose, debugging, scripts SQL, reestruturação de código, relatório. Toda a saída foi revista criticamente.
- **GitHub Copilot** — sugestões de autocompletion no backend Node.js.
- **Figma MCP Server** — extração de tokens de design via integração VS Code + Codex.

---

## Equipa

| Nome | Responsabilidades principais |
|---|---|
| **Simão** | Arquitetura, Room/DAOs, AppDatabase, Repositórios, Exportação CSV, Notificações, SyncWorker, Testes Unitários |
| **Gonçalo** | Retrofit/OkHttp, Interceptores JWT, SyncWorker, AuditLogger, Pesquisa, Testes Instrumentados |
| **Jorge** | Autenticação, Gestão de Utilizadores (Admin), Gestão de Tarefas (Gestor), Histórico, Validações |
| **Inês** | Onboarding, Perfil, Gestão de Projetos, Observações com Fotografias, UI/UX, Avaliações |

---

## Licença

Projeto académico desenvolvido no âmbito da Licenciatura em Engenharia Informática — Instituto Politécnico de Viana do Castelo.
