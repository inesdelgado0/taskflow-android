# TaskFlow – Gestão de Projetos e Tarefas

---

## Índice

1. [Descrição do Projeto](#descrição-do-projeto)
2. [Funcionalidades](#funcionalidades)
3. [Requisitos Funcionais](#requisitos-funcionais)
4. [Requisitos Não Funcionais](#requisitos-não-funcionais)
5. [Arquitetura](#arquitetura)
6. [Diagramas](#diagramas)
7. [Tecnologias e Dependências](#tecnologias-e-dependências)
8. [Estrutura do Projeto](#estrutura-do-projeto)
9. [Configuração e Instalação](#configuração-e-instalação)
10. [Como Usar a Aplicação](#como-usar-a-aplicação)
11. [API Reference](#api-reference)
12. [Base de Dados Local](#base-de-dados-local)
13. [Sincronização Offline](#sincronização-offline)
14. [Internacionalização (i18n)](#internacionalização-i18n)
15. [Testes](#testes)
16. [Geração do APK](#geração-do-apk)
17. [Gestão do Projeto (Trello)](#gestão-do-projeto-trello)
18. [Equipa](#equipa)

---

## Descrição do Projeto

**TaskFlow** é uma aplicação móvel Android desenvolvida em **Kotlin** no âmbito da unidade curricular de Computação Móvel. O objetivo é criar uma plataforma completa de **gestão de projetos e tarefas** para equipas, com suporte a perfis de utilizador, sincronização offline/online, visualização de estatísticas e notificações em tempo real.

### Visão Geral

A aplicação assenta num modelo **offline-first**: todos os dados são guardados localmente via **Room (SQLite)** e sincronizados automaticamente com uma **REST API** assim que houver conectividade. A lógica de negócio segue a arquitetura **MVVM com Clean Architecture**, garantindo separação de responsabilidades, testabilidade e escalabilidade. A interface é construída inteiramente com **Jetpack Compose**.

### Tipos de Perfil

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
- Iniciar sessão com JWT
- Logout

### Gestão de Perfil (todos os perfis)
- Consultar e editar dados pessoais: nome, username, email, fotografia e palavra-passe

### Administrador
- Criar, editar, remover e consultar projetos
- Criar, editar, remover e consultar contas de utilizadores e gestores de projeto
- Associar um gestor de projeto a cada projeto criado
- Exportar estatísticas (por utilizador / projeto / tarefa) em PDF ou CSV
- Pesquisar e filtrar projetos, tarefas e utilizadores

### Gestor de Projeto
- Criar, editar, remover e consultar tarefas associadas a projetos (título, descrição, prioridade, prazo, estado)
- Associar utilizadores a projetos e tarefas (relação N:M)
- Visualizar tarefas concluídas e por concluir por projeto
- Marcar projeto como concluído
- Avaliar a performance de cada utilizador (classificação numérica + comentário opcional)
- Exportar estatísticas (por utilizador / projeto / tarefa) em PDF ou CSV
- Pesquisar e filtrar projetos, tarefas e utilizadores

### Utilizador
- Consultar tarefas atribuídas
- Registar data, local, percentagem de conclusão e tempo dispensado em cada tarefa
- Adicionar observações às tarefas, com associação de fotografias (opcional)
- Marcar tarefa como concluída
- Visualizar lista de tarefas por realizar (pendentes)
- Visualizar histórico de tarefas concluídas
- Pesquisar e filtrar tarefas

### Funcionalidades Transversais
- **Offline-first**: operações guardadas localmente e sincronizadas automaticamente quando a conectividade for restabelecida
- **Notificações**: tarefas atribuídas, prazos próximos, alterações importantes, conclusão de sincronização
- **Pesquisa e filtragem**: por nome, estado, prioridade, datas ou utilizador associado
- **Validação e tratamento de erros**: mensagens claras em falhas de rede, autenticação inválida ou dados incorretos
- **Internacionalização**: Português (PT) e Inglês (EN)
- **Responsividade**: suporte a portrait e landscape via `WindowSizeClass`
- **Auditoria**: registo interno de autenticações, alterações de dados e operações de sincronização

---

## Requisitos Funcionais

| ID | Descrição |
|---|---|
| **RF01** | A aplicação deve apresentar um onboarding inicial no primeiro arranque e permitir ao utilizador criar conta, iniciar sessão e terminar sessão. |
| **RF02** | A aplicação deve permitir ao utilizador consultar e editar os seus dados de perfil, incluindo nome, username, email, fotografia e palavra-passe. |
| **RF03** | A aplicação deve suportar três perfis distintos — Administrador, Gestor de Projeto e Utilizador — com permissões específicas de acesso e funcionalidades próprias. |
| **RF04** | A aplicação deve permitir ao Administrador criar, editar, remover e consultar projetos, bem como associar um Gestor de Projeto a cada projeto criado. |
| **RF05** | A aplicação deve permitir ao Administrador criar, editar, remover e consultar contas de utilizadores e gestores de projeto. |
| **RF06** | A aplicação deve permitir ao Gestor de Projeto criar, editar, remover e consultar tarefas associadas a projetos, definindo título, descrição, prioridade, prazo e estado. |
| **RF07** | A aplicação deve permitir ao Gestor de Projeto associar utilizadores a projetos e tarefas (relação N:M em ambos os casos). |
| **RF08** | A aplicação deve permitir ao Gestor de Projeto visualizar tarefas concluídas e pendentes, marcar projetos como concluídos e avaliar o desempenho dos utilizadores (classificação numérica + comentário opcional). |
| **RF09** | A aplicação deve permitir ao Utilizador consultar tarefas atribuídas e registar data, local, percentagem de conclusão, tempo dispensado e marcar conclusão da tarefa. |
| **RF10** | A aplicação deve permitir ao Utilizador adicionar observações às tarefas, incluindo associação de fotografias. |
| **RF11** | A aplicação deve permitir ao Utilizador visualizar tarefas pendentes e histórico de tarefas concluídas. |
| **RF12** | A aplicação deve permitir ao Administrador e ao Gestor de Projeto consultar e exportar estatísticas por utilizador, projeto ou tarefa, em PDF ou CSV. |
| **RF13** | A aplicação deve sincronizar automaticamente os dados locais com a API remota quando a ligação à internet for restabelecida. |
| **RF14** | A aplicação deve permitir pesquisar e filtrar projetos, tarefas e utilizadores por nome, estado, prioridade, datas ou utilizador associado. |
| **RF15** | A aplicação deve enviar notificações relevantes: tarefas atribuídas, prazos próximos, alterações importantes e conclusão de sincronização. |
| **RF16** | A aplicação deve apresentar mensagens claras em situações de erro: falhas de rede, autenticação inválida ou dados incorretamente preenchidos. |

---

## Requisitos Não Funcionais

| ID | Descrição |
|---|---|
| **RNF01** | A aplicação deve ser desenvolvida para dispositivos Android, utilizando Kotlin como linguagem principal. |
| **RNF02** | A aplicação deve seguir o padrão MVVM com princípios de Clean Architecture, garantindo separação de responsabilidades, escalabilidade e facilidade de manutenção. |
| **RNF03** | A aplicação deve utilizar Room/SQLite para armazenamento local de dados. |
| **RNF04** | A aplicação deve comunicar com uma REST API através de HTTPS e utilizar autenticação baseada em JWT. |
| **RNF05** | A aplicação deve permitir utilização sem ligação à internet, armazenando localmente as operações até sincronização posterior. |
| **RNF06** | As operações principais devem apresentar tempo médio de resposta inferior a 2 segundos em condições normais de utilização. |
| **RNF07** | A aplicação deve manter estabilidade durante a utilização normal, minimizando falhas inesperadas e perda de dados. |
| **RNF08** | A aplicação deve suportar, no mínimo, os idiomas Português e Inglês. |
| **RNF09** | A aplicação deve adaptar corretamente a interface aos modos portrait e landscape, garantindo consistência visual e funcional. |
| **RNF10** | A aplicação deve manter registo interno de ações relevantes: autenticações, alterações de dados e operações de sincronização. |
| **RNF11** | A interface deve ser intuitiva, consistente e adequada aos diferentes perfis de utilizador. |
| **RNF12** | A aplicação deve permitir testes unitários e instrumentados, bem como evolução futura sem necessidade de reestruturação significativa. |

---

## Arquitetura

A aplicação segue o padrão **MVVM (Model-View-ViewModel)** com **Clean Architecture**, organizado em camadas:

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
                                    Response/Error → UiState (StateFlow) → Composable
```

---

## Diagramas

### Diagrama de Casos de Uso

```
                         ┌────────────────────────────────────────────────┐
                         │                   TaskFlow                     │
                         │                                                │
  ┌─────────────┐        │  ┌────────────────────────────────────────┐    │
  │             │──────────▶│ Criar/Editar/Remover Projetos         │   │
  │ Administra- │        │  └────────────────────────────────────────┘    │
  │    dor      │──────────▶│ Gerir Utilizadores e Gestores          │   │
  │             │        │  └────────────────────────────────────────┘   │
  └─────────────┘──────────▶│ Associar Gestor a Projeto              │   │
                         │  └────────────────────────────────────────┘   │
                         │  ┌────────────────────────────────────────┐   │
                         │  │ Exportar Estatísticas (PDF/CSV)        │   │
                         │  └────────────────────────────────────────┘   │
                         │  ┌────────────────────────────────────────┐   │
  ┌─────────────┐        │  │                                        │   │
  │  Gestor de  │──────────▶│ Criar/Gerir Tarefas                    │   │
  │  Projeto    │──────────▶│ Associar Utilizadores a Tarefas        │   │
  │             │──────────▶│ Ver progresso e avaliar performance    │   │
  └─────────────┘──────────▶│ Exportar Estatísticas (PDF/CSV)        │   │
                         │  └────────────────────────────────────────┘   │
                         │  ┌────────────────────────────────────────┐   │
  ┌─────────────┐        │  │                                        │   │
  │             │──────────▶│ Ver tarefas atribuídas                 │   │
  │ Utilizador  │──────────▶│ Registar progresso, local e tempo      │   │
  │             │──────────▶│ Adicionar fotos/observações            │   │
  └─────────────┘──────────▶│ Concluir tarefa / ver histórico        │   │
                         │  └────────────────────────────────────────┘   │
                         └────────────────────────────────────────────────┘
```

### Diagrama de Entidade-Relação (ER)

```
┌──────────┐       ┌──────────────┐       ┌──────────┐
│  User    │◄──────│ user_project │──────▶│ Project  │
│──────────│  N:M  │──────────────│  N:M  │──────────│
│ id       │       │ user_id (FK) │       │ id       │
│ name     │       │ project_id   │       │ name     │
│ username │       │   (FK)       │       │ desc     │
│ email    │       └──────────────┘       │ start_dt │
│ password │                              │ end_dt   │
│ photo    │                              │ status   │
│ role     │                              │ manager_id│
└────┬─────┘                              └────┬─────┘
     │                                         │ 1:N
     │                                    ┌────▼─────┐       ┌──────────────┐
     │                                    │  Task    │◄──────│  user_task   │
     │                                    │──────────│  N:M  │──────────────│
     │                                    │ id       │       │ user_id (FK) │
     │                                    │ project_id│      │ task_id (FK) │
     │                                    │ title    │       │ completion % │
     │                                    │ desc     │       │ time_spent   │
     │                                    │ deadline │       │ location     │
     │                                    │ priority │       │ date         │
     │                                    │ status   │       │ is_done      │
     │                                    └────┬─────┘       └──────────────┘
     │                                         │ 1:N
     │                                    ┌────▼──────────┐
     │                                    │  Observation  │
     │                                    │───────────────│
     │                                    │ id            │
     │                                    │ task_id (FK)  │
     │                                    │ user_id (FK)  │
     │                                    │ text          │
     │                                    │ photo_url     │
     │                                    │ created_at    │
     │                                    └───────────────┘
     │
     │  1:N
┌────▼──────────┐       ┌──────────────────┐
│  Evaluation   │       │   AuditLog       │
│───────────────│       │──────────────────│
│ id            │       │ id               │
│ evaluator_id  │       │ user_id (FK)     │
│   (FK)        │       │ action           │
│ evaluated_id  │       │ entity_type      │
│   (FK)        │       │ entity_id        │
│ project_id    │       │ timestamp        │
│   (FK)        │       │ details          │
│ rating        │       └──────────────────┘
│ comment       │
│ created_at    │       ┌──────────────────┐
└───────────────┘       │   SyncQueue      │
                        │──────────────────│
                        │ id               │
                        │ endpoint         │
                        │ method           │
                        │ payload (JSON)   │
                        │ created_at       │
                        │ retry_count      │
                        └──────────────────┘
```

### Diagrama de Navegação (App Flow)

```
SplashScreen
     │
     ├─── [Primeira vez] ──▶ OnboardingScreen (Sliders — Compose)
     │                              │
     │                              ▼
     └─── [Sessão existente] ─▶ NavHost (Compose Navigation)
                                    ├─▶ LoginScreen
                                    └─▶ RegisterScreen
                                              │
                          ┌───────────────────┼───────────────────┐
                          ▼                   ▼                   ▼
                  AdminNavGraph       ManagerNavGraph       UserNavGraph
                  ├─ ProjectsScreen   ├─ MyProjectsScreen   ├─ MyTasksScreen
                  ├─ UsersScreen      ├─ TaskManagerScreen  ├─ TaskDetailScreen
                  ├─ StatisticsScreen ├─ TeamViewScreen     ├─ ObservationsScreen
                  └─ ProfileScreen    ├─ StatisticsScreen   ├─ HistoryScreen
                                      └─ ProfileScreen      └─ ProfileScreen
```

---

## Tecnologias e Dependências

### Linguagem & Plataforma

| Tecnologia | Versão | Uso |
|---|---|---|
| Kotlin | 1.9.x | Linguagem principal (RNF01) |
| Android SDK | API 26–34 | Plataforma alvo |
| Android Studio | Hedgehog+ | IDE de desenvolvimento |
| Gradle | 8.x | Build system |

### UI & Navegação

| Biblioteca | Versão | Uso |
|---|---|---|
| Jetpack Compose BOM | 2024.02.x | Toolkit declarativo de UI (RNF11) |
| Material3 (Compose) | — (via BOM) | Componentes Material Design 3 |
| Compose Navigation | 2.7.x | Navegação entre Composables |
| Accompanist Pager | 0.32.x | Intro sliders / HorizontalPager (RF01) |
| Coil Compose | 2.6.x | Carregamento de imagens em Compose |
| Lottie Compose | 6.x | Animações (loading, empty states) |
| WindowSizeClass | — (via BOM) | Suporte portrait/landscape adaptativo (RNF09) |

### Arquitetura & Persistência

| Biblioteca | Versão | Uso |
|---|---|---|
| ViewModel + StateFlow | 2.7.x | MVVM / state management (RNF02) |
| Room | 2.6.x | Base de dados local SQLite (RNF03) |
| DataStore (Preferences) | 1.0.x | Armazenamento de preferências/tokens |
| Hilt (Dependency Injection) | 2.50.x | Injeção de dependências |
| Kotlin Coroutines | 1.7.x | Programação assíncrona |
| Flow | — | Streams reativos de dados |

### Rede & API

| Biblioteca | Versão | Uso |
|---|---|---|
| Retrofit | 2.9.x | Cliente HTTP REST (RNF04) |
| OkHttp + Logging Interceptor | 4.12.x | HTTP client + debug logs |
| Gson / Moshi | — | Serialização JSON |
| JWT Decoder | — | Leitura de tokens JWT (RNF04) |

### Background & Sync

| Biblioteca | Versão | Uso |
|---|---|---|
| WorkManager | 2.9.x | Sincronização offline em background (RF13, RNF05) |
| ConnectivityManager | — | Deteção de conectividade |

### Câmara & Ficheiros

| Biblioteca | Versão | Uso |
|---|---|---|
| CameraX | 1.3.x | Captura de fotografias (RF10) |
| Activity Result API | — | Permissões e resultados de intents |

### Exportação

| Biblioteca | Versão | Uso |
|---|---|---|
| iTextPDF / Apache POI | — | Geração de PDF e CSV para exportação (RF12) |

### Notificações

| Biblioteca | Versão | Uso |
|---|---|---|
| NotificationManager | — | Notificações locais (RF15) |
| WorkManager | 2.9.x | Agendamento de alertas de prazo |

### Testes

| Biblioteca | Versão | Uso |
|---|---|---|
| JUnit 4 | 4.13.x | Testes unitários (RNF12) |
| MockK | — | Mocking em testes |
| Compose UI Testing | — (via BOM) | Testes de UI instrumentados com `composeTestRule` |
| Turbine | — | Testes de Flow/Coroutines |

### `build.gradle.kts` – Dependências Principais

```kotlin
dependencies {
    // Jetpack Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Compose Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // WindowSizeClass (adaptive layouts)
    implementation("androidx.compose.material3:material3-window-size-class")

    // Accompanist (HorizontalPager para onboarding)
    implementation("com.google.accompanist:accompanist-pager:0.32.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.32.0")

    // Coil para imagens em Compose
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Lottie Compose
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Architecture
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

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
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

---

## Estrutura do Projeto

```
TaskFlow/
├── app/
│   ├── src/
│       ├── main/
│       │   ├── java/com/taskflow/app/
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── dao/               # DAOs do Room (UserDao, TaskDao, etc.)
│       │   │   │   │   ├── entity/            # Entidades Room
│       │   │   │   │   └── AppDatabase.kt     # Instância da BD Room
│       │   │   │   ├── remote/
│       │   │   │   │   ├── api/               # Interfaces Retrofit (UserApi, ProjectApi…)
│       │   │   │   │   ├── dto/               # Data Transfer Objects (request/response)
│       │   │   │   │   └── interceptor/       # AuthInterceptor (JWT token injection)
│       │   │   │   └── repository/            # Implementações dos repositórios
│       │   │   ├── domain/
│       │   │   │   ├── model/                 # Modelos de domínio (User, Project, Task…)
│       │   │   │   ├── repository/            # Interfaces dos repositórios
│       │   │   │   └── usecase/               # Casos de uso (LoginUseCase, CreateProjectUseCase…)
│       │   │   ├── ui/
│       │   │   │   ├── theme/                 # Theme.kt, Color.kt, Type.kt (Material3)
│       │   │   │   ├── navigation/            # NavGraph.kt, Routes.kt (Compose Navigation)
│       │   │   │   ├── auth/                  # LoginScreen.kt, RegisterScreen.kt
│       │   │   │   ├── onboarding/            # OnboardingScreen.kt (HorizontalPager)
│       │   │   │   ├── admin/                 # Screens e ViewModels do Administrador
│       │   │   │   │   ├── projects/          # ProjectsScreen.kt, ProjectDetailScreen.kt
│       │   │   │   │   ├── users/             # UsersScreen.kt, UserFormScreen.kt
│       │   │   │   │   └── statistics/        # StatisticsScreen.kt
│       │   │   │   ├── manager/               # Screens e ViewModels do Gestor de Projeto
│       │   │   │   │   ├── tasks/             # TasksScreen.kt, TaskFormScreen.kt
│       │   │   │   │   ├── team/              # TeamScreen.kt
│       │   │   │   │   └── statistics/        # ManagerStatisticsScreen.kt
│       │   │   │   ├── user/                  # Screens e ViewModels do Utilizador
│       │   │   │   │   ├── tasks/             # MyTasksScreen.kt, TaskDetailScreen.kt
│       │   │   │   │   ├── history/           # HistoryScreen.kt
│       │   │   │   │   └── observations/      # ObservationsScreen.kt
│       │   │   │   ├── profile/               # ProfileScreen.kt (todos os roles)
│       │   │   │   └── common/                # Composables reutilizáveis (botões, cards, dialogs…)
│       │   │   ├── di/                        # Módulos Hilt (NetworkModule, DatabaseModule…)
│       │   │   ├── sync/                      # Workers para sincronização offline (SyncWorker.kt)
│       │   │   ├── notification/              # Gestão de notificações locais
│       │   │   ├── audit/                     # Registo de ações e auditoria
│       │   │   ├── util/                      # Extensões Kotlin, helpers, constantes
│       │   │   └── TaskFlowApp.kt             # Application class
│       │   ├── res/
│       │   │   ├── values/
│       │   │   │   ├── strings.xml            # Strings PT (padrão)
│       │   │   │   ├── colors.xml             # Cores base (usadas também pelo tema Compose)
│       │   │   │   └── themes.xml             # Tema base da Activity (fundo para Compose)
│       │   │   ├── values-en/
│       │   │   │   └── strings.xml            # Strings EN
│       │   │   ├── drawable/                  # Ícones, fundos, vetores
│       │   │   ├── font/                      # Fontes personalizadas (se aplicável)
│       │   │   └── raw/                       # Animações Lottie (.json)
│       │   └── AndroidManifest.xml
│       ├── test/                              # Testes unitários (JUnit + MockK + Turbine)
│       └── androidTest/                       # Testes instrumentados (Compose UI Testing)
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

```bash
git clone https://github.com/<org>/taskflow-android.git
cd taskflow-android
```

### 2. Configurar Variáveis de Ambiente

Cria um ficheiro `local.properties` na raiz do projeto (não incluído no Git):

```properties
# local.properties
sdk.dir=/caminho/para/android/sdk

# URL base da API (desenvolvimento)
BASE_URL="https://api.taskflow.dev/"

# Chave de debug (opcional, para serviços externos)
MAPS_API_KEY=chave_opcional_para_google_maps
```

Estas variáveis são injetadas no `BuildConfig` via `build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        buildConfigField("String", "BASE_URL", localProperties["BASE_URL"].toString())
    }
}
```

### 3. Sincronizar Dependências

```bash
./gradlew build
```

Ou abre o projeto no Android Studio e clica em **"Sync Now"** quando solicitado.

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

```bash
./gradlew installDebug
```

Ou clicar no botão ▶ **Run** no Android Studio.

---

## Como Usar a Aplicação

### Primeiro Arranque

1. **Onboarding** — Aparece apenas na primeira execução. Navega pelos slides que apresentam as funcionalidades principais.
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
3. Avaliar cada membro da equipa (classificação numérica + comentário opcional)

### Perfil: Utilizador

#### Registar Trabalho numa Tarefa

1. **As Minhas Tarefas** → selecionar tarefa
2. Preencher:
    - **Data** de trabalho
    - **Local** (texto livre)
    - **Percentagem de conclusão**
    - **Tempo Dispensado** (horas:minutos)
3. Guardar

#### Adicionar Observação

1. Tarefa → separador **Observações** → Nova Observação
2. Escrever texto e/ou associar fotografia
3. Guardar

#### Marcar Tarefa como Concluída

1. Tarefa → **Marcar como Concluída**
2. Confirmar na caixa de diálogo

#### Ver Histórico

- **Tarefas por Realizar**: separador **Pendentes**
- **Tarefas Concluídas**: separador **Histórico**

---

## API Reference

A aplicação comunica com uma REST API via HTTPS. Todos os endpoints (exceto `/auth`) requerem autenticação Bearer JWT.

### Base URL

```
https://api.taskflow.dev/api/v1/
```

### Autenticação

```http
POST /auth/login
Content-Type: application/json

{ "email": "user@example.com", "password": "secret" }

→ 200 OK
{ "token": "eyJ...", "user": { "id": 1, "role": "ADMIN", ... } }
```

```http
POST /auth/register
→ 201 Created
```

### Projetos

```http
GET    /projects                   # Listar projetos (filtrado por role)
POST   /projects                   # Criar projeto (ADMIN)
GET    /projects/{id}              # Detalhes do projeto
PUT    /projects/{id}              # Editar projeto (ADMIN)
DELETE /projects/{id}              # Remover projeto (ADMIN)
PUT    /projects/{id}/complete     # Concluir projeto (GESTOR)
```

### Tarefas

```http
GET    /projects/{id}/tasks        # Listar tarefas do projeto
POST   /projects/{id}/tasks        # Criar tarefa (GESTOR)
GET    /tasks/{id}                 # Detalhes da tarefa
PUT    /tasks/{id}                 # Editar tarefa (GESTOR)
DELETE /tasks/{id}                 # Remover tarefa (GESTOR)
PUT    /tasks/{id}/complete        # Concluir tarefa (UTILIZADOR)
POST   /tasks/{id}/progress        # Registar progresso (UTILIZADOR)
```

### Utilizadores

```http
GET    /users                      # Listar utilizadores (ADMIN)
POST   /users                      # Criar utilizador (ADMIN)
GET    /users/{id}                 # Detalhes do utilizador
PUT    /users/{id}                 # Editar utilizador
DELETE /users/{id}                 # Remover utilizador (ADMIN)
PUT    /users/{id}/evaluate        # Avaliar utilizador (GESTOR)
```

### Observações

```http
GET    /tasks/{id}/observations    # Listar observações
POST   /tasks/{id}/observations    # Criar observação (com foto opcional)
DELETE /observations/{id}          # Remover observação
```

### Estatísticas

```http
GET    /stats/users/{id}           # Stats por utilizador
GET    /stats/projects/{id}        # Stats por projeto
GET    /stats/tasks/{id}           # Stats por tarefa
GET    /stats/export?format=pdf    # Exportar em PDF
GET    /stats/export?format=csv    # Exportar em CSV
```

---

## Base de Dados Local

A aplicação usa **Room** para persistência local (RNF03). Abaixo estão as principais tabelas:

| Tabela | Descrição |
|---|---|
| `users` | Dados dos utilizadores e seus perfis |
| `projects` | Projetos criados pelo Administrador |
| `tasks` | Tarefas associadas a projetos |
| `user_project` | Relação N:M utilizador–projeto |
| `user_task` | Relação N:M utilizador–tarefa + dados de execução |
| `observations` | Observações com fotografia por tarefa |
| `evaluations` | Avaliações de desempenho por projeto |
| `audit_log` | Registo interno de ações relevantes (RNF10) |
| `sync_queue` | Fila de operações pendentes para sincronização (RNF05) |

A tabela `sync_queue` é o coração do mecanismo offline: cada operação CRUD feita sem conectividade é inserida nesta fila com o payload JSON e o endpoint destino. A tabela `audit_log` regista autenticações, alterações de dados e operações de sincronização para fins de auditoria.

---

## Sincronização Offline

```
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
│        ↓                                              │
│  Notificação enviada ao utilizador (RF15)             │
└───────────────────────────────────────────────────────┘
```

O estado de sincronização é visível na UI com um ícone de nuvem na barra de estado:
- ☁ (cinzento) — sem ligação
- ↻ (animado) — a sincronizar
- ✓ (verde) — sincronizado

---

## Internacionalização (i18n)

| Idioma | Código | Ficheiro |
|---|---|---|
| Português | `pt` (padrão) | `res/values/strings.xml` |
| Inglês | `en` | `res/values-en/strings.xml` |

A língua é selecionada automaticamente com base na configuração do dispositivo. Pode também ser alterada manualmente nas definições da aplicação, persistindo via DataStore.

O suporte a portrait e landscape é gerido programaticamente em Compose com `WindowSizeClass` (RNF09) — não existem pastas `res/layout-land/`.

---

## Testes

### Executar Testes Unitários

```bash
./gradlew test
```

Cobertura de testes (RNF12):
- UseCases (login, criação de projeto, etc.)
- ViewModels (estado e transformações)
- Repositórios (lógica de cache/remote)
- Room DAOs

### Executar Testes Instrumentados

```bash
./gradlew connectedAndroidTest
```

Requer dispositivo/emulador ligado. Cobre fluxos de UI críticos via **Compose UI Testing** (`composeTestRule`):
- Login com credenciais válidas e inválidas
- Criação de projeto (fluxo admin)
- Registo de progresso em tarefa (fluxo utilizador)

Exemplo de teste Compose:

```kotlin
@get:Rule
val composeTestRule = createAndroidComposeRule<MainActivity>()

@Test
fun loginScreen_displaysErrorOnInvalidCredentials() {
    composeTestRule.onNodeWithTag("email_field").performTextInput("invalid@email.com")
    composeTestRule.onNodeWithTag("password_field").performTextInput("wrongpass")
    composeTestRule.onNodeWithTag("login_button").performClick()
    composeTestRule.onNodeWithText("Credenciais inválidas").assertIsDisplayed()
}
```

---

## Geração do APK

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK (assinado)

1. Criar keystore (apenas uma vez):

```bash
keytool -genkey -v -keystore taskflow.keystore \
  -alias taskflow -keyalg RSA -keysize 2048 -validity 10000
```

2. Configurar no `build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("taskflow.keystore")
        storePassword = System.getenv("KEYSTORE_PASS")
        keyAlias = "taskflow"
        keyPassword = System.getenv("KEY_PASS")
    }
}
```

3. Gerar APK:

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

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
| Simão | Arquitetura, Room, Notificações, Exportação, Testes Unitários | [email] |
| Gonçalo | Rede, Sincronização, Auditoria, Pesquisa, Testes Instrumentados | [email] |
| Jorge | Auth, Gestão de Utilizadores/Tarefas, Histórico, Validações | [email] |
| Inês | Onboarding, Perfil, Gestão de Projetos, Observações, UI/UX | [email] |

---

## Licença

Este projeto é desenvolvido no âmbito académico da licenciatura em [Engenharia Informática] no [Instituto Politécnico de Viana do Castelo].