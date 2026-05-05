# TaskFlow – Gestão de Projetos e Tarefas

---

## Índice

1. [Descrição do Projeto]
2. [Funcionalidades]
3. [Arquitetura]
4. [Diagramas]
5. [Tecnologias e Dependências]
6. [Estrutura do Projeto]
7. [Configuração e Instalação]
8. [Como Usar a Aplicação]
9. [API Reference]
10. [Base de Dados Local]
11. [Sincronização Offline]
12. [Internacionalização (i18n)]
13. [Testes]
14. [Geração do APK]
15. [Gestão do Projeto (Trello)]
16. [Equipa]

---

## Descrição do Projeto

**TaskFlow** é uma aplicação móvel Android desenvolvida em **Kotlin** no âmbito da unidade curricular de Computação Móvel. O objetivo é criar uma plataforma completa de **gestão de projetos e tarefas** para equipas, com suporte a perfis de utilizador, sincronização offline/online e visualização de estatísticas.

### Visão Geral

A aplicação assenta num modelo **offline-first**: todos os dados são guardados localmente via **Room (SQLite)** e sincronizados com uma **REST API** assim que houver conectividade. A lógica de negócio segue a arquitetura **MVVM**, garantindo separação de responsabilidades, testabilidade e escalabilidade.

### Tipos de Perfil

| Perfil | Responsabilidades principais |

| **Administrador** | Gerir utilizadores, projetos e exporta estatísticas globais |
| **Gestor de Projeto** | Gerir tarefas, associa utilizadores e avalia performance |
| **Utilizador** | Executa tarefas, regista progresso e adiciona observações |

---

## Funcionalidades

### Onboarding
- Intro sliders (apresentação da app )
- Ecrã de boas-vindas com opção de Login ou Registo

### Autenticação & Perfil
- Criar conta (nome, username, email, password, fotografia)
- Iniciar sessão com JWT
- Gestão de perfil (editar dados pessoais, alterar fotografia, mudar password)
- Logout 

### Administrador
- Criar, editar e remover projetos
- Criar, editar e remover contas de utilizadores e gestores de projeto
- Associar um gestor de projeto a um projeto
- Exportar estatísticas (por utilizador / projeto / tarefa) 

### Gestor de Projeto
- Associar tarefas a projetos (nome, descrição, prazo)
- Associar utilizadores a projetos e tarefas 
- Visualizar tarefas concluídas e por concluir por projeto
- Marcar projeto como concluído
- Avaliar a performance de cada utilizador
- Exportar estatísticas (por utilizador / projeto / tarefa)

### Utilizador
- Registar data, local, taxa de conclusão e tempo dispensado em cada tarefa
- Adicionar observações às tarefas
- Marcar tarefa como concluída
- Visualizar lista de tarefas por realizar
- Visualizar histórico de tarefas concluídas

### Offline
- Guardar dados localmente quando sem ligação
- Sincronização automática ao recuperar conectividade (WorkManager)
- Indicador visual de estado de sincronização

### Internacionalização
- Suporte completo a Português (PT) e Inglês (EN)
- Layouts adaptados a portrait e landscape

---

## Arquitetura

A aplicação segue o padrão **MVVM (Model-View-ViewModel)** com **Clean Architecture**, organizado em camadas:

┌─────────────────────────────────────────────────┐
│                   UI Layer                      │
│        (Activities, Fragments, Adapters)        │
│              Observa LiveData/StateFlow         │
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


### Fluxo de Dados

User Action → ViewModel → UseCase → Repository
                                       ├─ Room (local cache)
                                       └─ Retrofit (API remota)
                                              ↓
                                    Response/Error → ViewModel → UI State → View

## Diagramas

### Diagrama de Casos de Uso


                         ┌────────────────────────────────────────────────┐
                         │                   TaskFlow                     │
                         │                                                │
  ┌─────────────┐        │  ┌────────────────────────────────────────┐   │
  │             │──────────▶│ Criar/Editar/Remover Projetos          │   │
  │ Administra- │        │  └────────────────────────────────────────┘   │
  │    dor      │──────────▶│ Gerir Utilizadores e Gestores          │   │
  │             │        │  └────────────────────────────────────────┘   │
  └─────────────┘──────────▶│ Associar Gestor a Projeto              │   │
                         │  └────────────────────────────────────────┘   │
                         │  ┌────────────────────────────────────────┐   │
  ┌─────────────┐        │  │                                        │   │
  │  Gestor de  │──────────▶│ Criar/Gerir Tarefas                    │   │
  │  Projeto    │──────────▶│ Associar Utilizadores a Tarefas        │   │
  │             │──────────▶│ Ver progresso e avaliar performance    │   │
  └─────────────┘──────────▶│ Exportar Estatísticas                  │   │
                         │  └────────────────────────────────────────┘   │
                         │  ┌────────────────────────────────────────┐   │
  ┌─────────────┐        │  │                                        │   │
  │             │──────────▶│ Ver tarefas atribuídas                 │   │
  │ Utilizador  │──────────▶│ Registar progresso e tempo             │   │
  │             │──────────▶│ Adicionar fotos/observações            │   │
  └─────────────┘──────────▶│ Concluir tarefa                       │   │
                         │  └────────────────────────────────────────┘   │
                         └────────────────────────────────────────────────┘


### Diagrama de Entidade-Relação (ER)

┌──────────┐       ┌──────────┐       ┌──────────┐
│  User    │◄──────│UserProject│──────▶│ Project  │
│──────────│  N:N  │──────────│  N:N  │──────────│
│ id       │       │ user_id  │       │ id       │
│ name     │       │ project_id│      │ name     │
│ username │       │ role     │       │ desc     │
│ email    │       └──────────┘       │ start_dt │
│ password │                          │ end_dt   │
│ photo    │                          │ status   │
│ role     │                          │ manager_id│
└──────────┘                          └────┬─────┘
                                           │ 1:N
                                      ┌────▼─────┐       ┌──────────┐
                                      │  Task    │◄──────│UserTask  │
                                      │──────────│  N:N  │──────────│
                                      │ id       │       │ user_id  │
                                      │ project_id│      │ task_id  │
                                      │ title    │       │ progress │
                                      │ desc     │       │ time_sp. │
                                      │ deadline │       │ location │
                                      │ priority │       │ date     │
                                      │ status   │       └──────────┘
                                      └──────────┘
                                           │ 1:N
                                      ┌────▼─────┐
                                      │Observation│
                                      │──────────│
                                      │ id       │
                                      │ task_id  │
                                      │ user_id  │
                                      │ text     │
                                      │ photo_url│
                                      │ created_at│
                                      └──────────┘
### Diagrama de Navegação (App Flow)

SplashScreen
     │
     ├─── [Primeira vez] ──▶ OnboardingActivity (Sliders)
     │                              │
     │                              ▼
     └─── [Sessão existente] ─▶ AuthActivity
                                    ├─▶ LoginFragment
                                    └─▶ RegisterFragment
                                              │
                          ┌───────────────────┼───────────────────┐
                          ▼                   ▼                   ▼
                  AdminActivity      ManagerActivity       UserActivity
                  ├─ Projects        ├─ MyProjects          ├─ MyTasks
                  ├─ Users           ├─ TaskManager         ├─ TaskDetail
                  ├─ Statistics      ├─ TeamView            ├─ Observations
                  └─ Profile         ├─ Statistics          └─ Profile
                                     └─ Profile

## Tecnologias e Dependências

### Linguagem & Plataforma
| Tecnologia | Versão | Uso |
|---|---|---|
| Kotlin | 1.9.x | Linguagem principal |
| Android SDK | API 26–34 | Plataforma alvo |
| Android Studio | Hedgehog+ | IDE de desenvolvimento |
| Gradle | 8.x | Build system |

### UI & Navegação
| Biblioteca | Versão | Uso |
|---|---|---|
| Material Design 3 | 1.11.x | Componentes UI |
| Navigation Component | 2.7.x | Navegação entre fragments |
| ViewPager2 | 1.0.x | Intro sliders |
| Glide | 4.16.x | Carregamento de imagens |
| CircleImageView | 3.1.x | Foto de perfil circular |
| Lottie | 6.x | Animações (loading, empty states) |

### Arquitetura & Persistência
| Biblioteca | Versão | Uso |
|---|---|---|
| ViewModel + LiveData | 2.7.x | MVVM / state management |
| Room | 2.6.x | Base de dados local SQLite |
| DataStore (Preferences) | 1.0.x | Armazenamento de preferências/tokens |
| Hilt (Dependency Injection) | 2.50.x | Injeção de dependências |
| Kotlin Coroutines | 1.7.x | Programação assíncrona |
| Flow | — | Streams reativos de dados |

### Rede & API
| Biblioteca | Versão | Uso |
|---|---|---|
| Retrofit | 2.9.x | Cliente HTTP REST |
| OkHttp + Logging Interceptor | 4.12.x | HTTP client + debug logs |
| Gson / Moshi | — | Serialização JSON |
| JWT Decoder | — | Leitura de tokens JWT |

### Background & Sync
| Biblioteca | Versão | Uso |
|---|---|---|
| WorkManager | 2.9.x | Sincronização offline em background |
| ConnectivityManager | — | Deteção de conectividade |

### Câmara & Ficheiros
| Biblioteca | Versão | Uso |
|---|---|---|
| CameraX | 1.3.x | Captura de fotografias |
| Activity Result API | — | Permissões e resultados de intents |

### Exportação
| Biblioteca | Versão | Uso |
|---|---|---|
| iTextPDF / Apache POI | — | Geração de PDF e CSV para exportação |

### Testes
| Biblioteca | Versão | Uso |
|---|---|---|
| JUnit 4 | 4.13.x | Testes unitários |
| Mockito / MockK | — | Mocking em testes |
| Espresso | 3.5.x | Testes de UI instrumentados |
| Turbine | — | Testes de Flow/Coroutines |

### `build.gradle.kts` – Dependências Principais

kotlin
dependencies {
    // UI
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.airbnb.android:lottie:6.1.0")

    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Camera
    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.3.1")

    // PDF Export
    implementation("com.itextpdf:itext7-core:7.2.5")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
}

## Estrutura do Projeto

TaskFlow/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/taskflow/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/           # DAOs do Room (UserDao, TaskDao, etc.)
│   │   │   │   │   │   ├── entity/        # Entidades Room
│   │   │   │   │   │   └── AppDatabase.kt # Instância da BD Room
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/           # Interfaces Retrofit (UserApi, ProjectApi…)
│   │   │   │   │   │   ├── dto/           # Data Transfer Objects (request/response)
│   │   │   │   │   │   └── interceptor/   # AuthInterceptor (JWT token injection)
│   │   │   │   │   └── repository/        # Implementações dos repositórios
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/             # Modelos de domínio (User, Project, Task…)
│   │   │   │   │   ├── repository/        # Interfaces dos repositórios
│   │   │   │   │   └── usecase/           # Casos de uso (LoginUseCase, CreateProjectUseCase…)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/              # Login, Registo
│   │   │   │   │   ├── onboarding/        # Intro Sliders
│   │   │   │   │   ├── admin/             # Ecrãs do Administrador
│   │   │   │   │   │   ├── projects/
│   │   │   │   │   │   ├── users/
│   │   │   │   │   │   └── statistics/
│   │   │   │   │   ├── manager/           # Ecrãs do Gestor de Projeto
│   │   │   │   │   │   ├── tasks/
│   │   │   │   │   │   ├── team/
│   │   │   │   │   │   └── statistics/
│   │   │   │   │   ├── user/              # Ecrãs do Utilizador
│   │   │   │   │   │   ├── tasks/
│   │   │   │   │   │   ├── history/
│   │   │   │   │   │   └── observations/
│   │   │   │   │   ├── profile/           # Gestão de perfil (todos os roles)
│   │   │   │   │   └── common/            # Componentes reutilizáveis
│   │   │   │   ├── di/                    # Módulos Hilt (NetworkModule, DatabaseModule…)
│   │   │   │   ├── sync/                  # Workers para sincronização offline
│   │   │   │   ├── util/                  # Extensões Kotlin, helpers, constantes
│   │   │   │   └── TaskFlowApp.kt         # Application class
│   │   │   ├── res/
│   │   │   │   ├── layout/                # XMLs de layout
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml        # Strings PT (padrão)
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   ├── values-en/
│   │   │   │   │   └── strings.xml        # Strings EN
│   │   │   │   ├── drawable/              # Ícones, fundos, vetores
│   │   │   │   ├── navigation/            # Grafos de navegação (nav_graph.xml)
│   │   │   │   ├── layout-land/           # Layouts específicos para landscape
│   │   │   │   └── raw/                   # Animações Lottie (.json)
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                          # Testes unitários (JUnit + MockK)
│   │   └── androidTest/                   # Testes instrumentados (Espresso)
├── docs/
│   ├── assets/                            # Imagens para o README
│   ├── api/                               # Documentação da API (Swagger/Postman)
│   └── wireframes/                        # Mockups e wireframes
├── build.gradle.kts
├── settings.gradle.kts
└── README.md

## Configuração e Instalação

### Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Android Studio | Hedgehog (2023.1.1) ou superior |
| JDK | 17+ |
| Android SDK | API 26 (Android 8.0) |
| Git | 2.x |
| Gradle | 8.x (gerido pelo wrapper) |

### 1. Clonar o Repositório

bash
git clone https://github.com/inesdelgado0/taskflow-android.git
cd taskflow-android

### 2. Configurar Variáveis de Ambiente

Cria um ficheiro "local.properties" na raiz do projeto (não incluído no Git):

properties
# local.properties
sdk.dir=/caminho/para/android/sdk

# URL base da API (desenvolvimento)
BASE_URL="https://api.taskflow.dev/"

# Chave de debug (opcional, para serviços externos)
MAPS_API_KEY=chave_opcional_para_google_maps



Estas variáveis são injetadas no `BuildConfig` via `build.gradle.kts`:

kotlin
android {
    defaultConfig {
        buildConfigField("String", "BASE_URL", localProperties["BASE_URL"].toString())
    }
}


### 3. Sincronizar Dependências
bash
./gradlew build


Ou simplesmente abre o projeto no Android Studio e clica em **"Sync Now"** quando solicitado.

### 4. Configurar Dispositivo ou Emulador

**Emulador:**
1. Android Studio → Device Manager → Create Virtual Device
2. Selecionar Pixel 6 (ou similar) com API 34
3. Iniciar o AVD

**Dispositivo Físico:**
1. Ativar **Opções de Programador** no Android
2. Ativar **Depuração USB**
3. Ligar via USB e aceitar a ligação no dispositivo

### 5. Executar a Aplicação

bash
./gradlew installDebug

Ou clicar no botão ▶ **Run** no Android Studio.

## Como Usar a Aplicação

### Primeiro Arranque

1. **Onboarding** — Aparece apenas na primeira execução. Navega pelos 3–4 slides que apresentam as funcionalidades principais.
2. **Registo** — Cria uma conta com nome, username, e-mail e password. A fotografia de perfil é opcional.
3. **Login** — Autentica com e-mail e password. O token JWT é guardado de forma segura via DataStore.


### Gestão de Perfil (todos os perfis)

Acesso: Menu lateral ou ícone de perfil no topo.

- Editar nome e username
- Alterar e-mail (requer confirmação)
- Mudar fotografia (câmara ou galeria)
- Alterar password (requer password atual)

### Perfil: Administrador

#### Gerir Projetos
| Ação | Como fazer |
|---|---|
| Criar projeto | Dashboard → Novo Projeto → preencher nome, descrição, datas, atribuir gestor |
| Editar projeto | Lista de projetos → selecionar → Editar |
| Remover projeto | Lista de projetos → selecionar → Eliminar (confirmação obrigatória) |

#### Gerir Utilizadores
| Ação | Como fazer |
|---|---|
| Criar conta | Utilizadores → Novo → preencher dados e atribuir perfil |
| Editar conta | Utilizadores → selecionar → Editar |
| Remover conta | Utilizadores → selecionar → Eliminar |

#### Exportar Estatísticas
1. Dashboard → **Estatísticas**
2. Escolher filtro: **Por Utilizador / Por Projeto / Por Tarefa**
3. Definir intervalo de datas
4. Exportar como **PDF** ou **CSV**

### Perfil: Gestor de Projeto

#### Gerir Tarefas
| Ação | Como fazer |
|---|---|
| Criar tarefa | Projeto → Tarefas → Nova Tarefa → preencher título, descrição, prazo, prioridade |
| Associar utilizadores | Tarefa → Adicionar Membro → selecionar da equipa |
| Ver progresso | Projeto → separador **Em Progresso / Concluídas** |

#### Concluir Projeto e Avaliar Equipa
1. Projeto → **Concluir Projeto**
2. Confirmar que todas as tarefas críticas estão concluídas
3. Avaliar cada membro da equipa (1–5 estrelas + comentário opcional)

### Perfil: Utilizador

#### Registar Trabalho numa Tarefa
1. **As Minhas Tarefas** → selecionar tarefa
2. Preencher:
   - **Data** de trabalho
   - **Local** (texto livre ou GPS)
   - **Tempo Dispensado** (horas:minutos)
3. Guardar

#### Adicionar Observação
1. Tarefa → separador **Observações** → Nova Observação
2. Escrever texto
3. Guardar

#### Marcar Tarefa como Concluída
1. Tarefa → **Marcar como Concluída**
2. Confirmar na caixa de diálogo

#### Ver Histórico
- **Tarefas por Realizar**: separador **Pendentes**
- **Tarefas Concluídas**: separador **Histórico**

## API Reference

A aplicação comunica com uma REST API via HTTPS. Todos os endpoints (exceto `/auth`) requerem autenticação Bearer JWT.

### Base URL
https://api.taskflow.dev/api/v1/

### Autenticação
http
POST /auth/login
Content-Type: application/json

{ "email": "user@example.com", "password": "secret" }

→ 200 OK
{ "token": "eyJ...", "user": { "id": 1, "role": "ADMIN", ... } }


http
POST /auth/register
→ 201 Created


### Projetos
http
GET    /projects           # Listar projetos (filtrado por role)
POST   /projects           # Criar projeto (ADMIN)
GET    /projects/{id}      # Detalhes do projeto
PUT    /projects/{id}      # Editar projeto (ADMIN)
DELETE /projects/{id}      # Remover projeto (ADMIN)
PUT    /projects/{id}/complete   # Concluir projeto (GESTOR)

### Tarefas
http
GET    /projects/{id}/tasks          # Listar tarefas do projeto
POST   /projects/{id}/tasks          # Criar tarefa (GESTOR)
GET    /tasks/{id}                   # Detalhes da tarefa
PUT    /tasks/{id}                   # Editar tarefa (GESTOR)
DELETE /tasks/{id}                   # Remover tarefa (GESTOR)
PUT    /tasks/{id}/complete          # Concluir tarefa (UTILIZADOR)
POST   /tasks/{id}/progress          # Registar progresso (UTILIZADOR)


### Utilizadores
http
GET    /users                        # Listar utilizadores (ADMIN)
POST   /users                        # Criar utilizador (ADMIN)
GET    /users/{id}                   # Detalhes do utilizador
PUT    /users/{id}                   # Editar utilizador
DELETE /users/{id}                   # Remover utilizador (ADMIN)
PUT    /users/{id}/evaluate          # Avaliar utilizador (GESTOR)


### Observações
http
GET    /tasks/{id}/observations      # Listar observações
POST   /tasks/{id}/observations      # Criar observação (com foto opcional)
DELETE /observations/{id}            # Remover observação


### Estatísticas
http
GET    /stats/users/{id}             # Stats por utilizador
GET    /stats/projects/{id}          # Stats por projeto
GET    /stats/tasks/{id}             # Stats por tarefa
GET    /stats/export?format=pdf      # Exportar em PDF
GET    /stats/export?format=csv      # Exportar em CSV


## Base de Dados Local

A aplicação usa **Room** para persistência local. Abaixo estão as principais tabelas:

| Tabela | Descrição |
|---|---|
| `users` | Dados dos utilizadores |
| `projects` | Projetos |
| `tasks` | Tarefas associadas a projetos |
| `user_project` | Relação N:N utilizador–projeto |
| `user_task` | Relação N:N utilizador–tarefa + progresso |
| `observations` | Observações com fotografia por tarefa |
| `sync_queue` | Fila de operações pendentes para sincronização |

A tabela `sync_queue` é o coração do mecanismo offline: cada operação CRUD feita sem conectividade é inserida nesta fila com o payload JSON e o endpoint destino.

## Sincronização Offline


┌───────────────────────────────────────────────────────┐
│  Utilizador executa ação (sem internet)               │
│        ↓                                              │
│  Operação guardada em Room (sync_queue)               │
│        ↓                                              │
│  ConnectivityManager deteta ligação restabelecida     │
│        ↓                                              │
│  WorkManager dispara SyncWorker                       │
│        ↓                                              │
│  SyncWorker processa fila em ordem FIFO               │
│    ├─ Sucesso → remove da fila                        │
│    └─ Falha   → recoloca com retry (backoff)          │
└───────────────────────────────────────────────────────┘

O estado de sincronização é visível na UI com um ícone de nuvem na barra de estado:
- (cinzento) — sem ligação
- (animado) — a sincronizar
- (verde) — sincronizado

---

## Internacionalização (i18n)

| Idioma | Código | Ficheiro |
|---|---|---|
| Português | `pt` (padrão) | `res/values/strings.xml` |
| Inglês | `en` | `res/values-en/strings.xml` |

A língua é selecionada automaticamente com base na configuração do dispositivo. Pode também ser alterada manualmente nas definições da aplicação, persistindo via DataStore.

Todos os layouts têm versão **portrait** (padrão) e **landscape** em `res/layout-land/`.


## Testes

### Executar Testes Unitários

bash
./gradlew test


Cobertura de testes:
- UseCases (login, criação de projeto, etc.)
- ViewModels (estado e transformações)
- Repositórios (lógica de cache/remote)
- Room DAOs

### Executar Testes Instrumentados

bash
./gradlew connectedAndroidTest


Requer dispositivo/emulador ligado. Cobre fluxos de UI críticos via Espresso:
- Login com credenciais válidas e inválidas
- Criação de projeto (fluxo admin)
- Registo de progresso em tarefa (fluxo utilizador)


## Geração do APK

### Debug APK

bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

### Release APK (assinado)

1. Criar keystore (apenas uma vez):
bash
keytool -genkey -v -keystore taskflow.keystore \
  -alias taskflow -keyalg RSA -keysize 2048 -validity 10000


2. Configurar no `build.gradle.kts`:
kotlin
signingConfigs {
    create("release") {
        storeFile = file("taskflow.keystore")
        storePassword = System.getenv("KEYSTORE_PASS")
        keyAlias = "taskflow"
        keyPassword = System.getenv("KEY_PASS")
    }
}


3. Gerar APK:
bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk

## Gestão do Projeto (Trello)

O projeto é gerido no **Trello** com o seguinte quadro:

[https://trello.com/b/taskflow-android](https://trello.com/b/taskflow-android) *(link a atualizar)*

### Estrutura do Quadro

| Coluna | Descrição |
|---|---|
| **Backlog** | Todas as funcionalidades planeadas |
| **A Fazer** | Sprint atual – tarefas selecionadas |
| **Em Progresso** | Tarefas em desenvolvimento ativo |
| **Em Revisão** | Pull requests abertos / a rever |
| **Concluído** | Tarefas entregues e validadas |

---

## Equipa

| Nome | Papel | Contacto |
|---|---|---|
| [Nome 1] | Desenvolvimento Android / Arquitetura | [email] |
| [Nome 2] | Desenvolvimento Android / UI | [email] |
| [Nome 3] | Backend / API / Documentação | [email] |

---

## Licença

Este projeto é desenvolvido no âmbito académico da licenciatura em [Engenharia Informática] no [Instituto Politécnico de Viana do Castelo].
