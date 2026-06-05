-- =============================================================
-- TaskFlow – Seed Data para Supabase
-- =============================================================
-- Nota: timestamps em Unix epoch (milissegundos), conforme o modelo.
-- Passwords são bcrypt hashes de "password123" (para testes).
-- =============================================================

-- Limpar dados existentes (respeitando ordem das FK)
TRUNCATE TABLE public.device_tokens, public.sync_queue, public.audit_log, public.evaluations, public.observations,
              public.user_task, public.user_project, public.tasks, public.projects,
              public.user_roles, public.roles, public.users
RESTART IDENTITY CASCADE;

-- =============================================================
-- 1. ROLES
-- =============================================================
INSERT INTO public.roles (id, code, description, created_at) OVERRIDING SYSTEM VALUE VALUES
  (1, 'ADMIN',   'Administrador do sistema com acesso total',        1700000000000),
  (2, 'MANAGER', 'Gestor de projetos e equipas',                     1700000000000),
  (3, 'USER',    'Utilizador standard com acesso às suas tarefas',   1700000000000);

-- =============================================================
-- 2. USERS
-- =============================================================
INSERT INTO public.users (id, name, username, email, password_hash, photo_url, role, is_active, created_at, updated_at) OVERRIDING SYSTEM VALUE VALUES
  (1, 'Ana Ferreira',    'ana.ferreira',    'ana@taskflow.pt',     '$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'ADMIN',   TRUE,  1700000000000, 1700000000000),
  (2, 'Bruno Carvalho',  'bruno.carvalho',  'bruno@taskflow.pt',   '$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'MANAGER', TRUE,  1700100000000, 1700100000000),
  (3, 'Catarina Sousa',  'catarina.sousa',  'catarina@taskflow.pt','$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'MANAGER', TRUE,  1700200000000, 1700200000000),
  (4, 'Diogo Martins',   'diogo.martins',   'diogo@taskflow.pt',   '$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'USER',    TRUE,  1700300000000, 1700300000000),
  (5, 'Eva Rodrigues',   'eva.rodrigues',   'eva@taskflow.pt',     '$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'USER',    TRUE,  1700400000000, 1700400000000),
  (6, 'Filipe Costa',    'filipe.costa',    'filipe@taskflow.pt',  '$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'USER',    TRUE,  1700500000000, 1700500000000),
  (7, 'Gabriela Nunes',  'gabriela.nunes',  'gabriela@taskflow.pt','$2b$12$Ad8G0pWUuKkqYZfAygVAleQwwLsy8MpiwnnSucBx0uxE/yoEMli6K', NULL, 'USER',    FALSE, 1700600000000, 1700600000000); -- conta inativa

-- =============================================================
-- 3. USER_ROLES
-- =============================================================
INSERT INTO public.user_roles (user_id, role_id, assigned_at) VALUES
  -- Ana: ADMIN
  (1, 1, 1700000000000),
  -- Bruno: MANAGER + USER (acumula roles)
  (2, 2, 1700100000000),
  (2, 3, 1700100000000),
  -- Catarina: MANAGER
  (3, 2, 1700200000000),
  -- Diogo: USER
  (4, 3, 1700300000000),
  -- Eva: USER
  (5, 3, 1700400000000),
  -- Filipe: USER
  (6, 3, 1700500000000),
  -- Gabriela: USER (inativa)
  (7, 3, 1700600000000);

-- =============================================================
-- 4. PROJECTS
-- =============================================================
INSERT INTO public.projects (id, name, description, start_date, end_date, status, manager_id, created_by, created_at, updated_at) OVERRIDING SYSTEM VALUE VALUES
  (1, 'App Móvel TaskFlow',
      'Desenvolvimento da aplicação móvel Android para gestão de tarefas de campo.',
      1700000000000, 1710000000000, 'ACTIVE',    2, 1, 1700000000000, 1700000000000),

  (2, 'Portal de Administração',
      'Painel web para administração de utilizadores, projetos e relatórios.',
      1700500000000, 1712000000000, 'ACTIVE',    3, 1, 1700500000000, 1700500000000),

  (3, 'Integração API Remota',
      'Implementação dos endpoints REST e mecanismo de sincronização offline.',
      1699000000000, 1702000000000, 'COMPLETED', 2, 1, 1699000000000, 1702000000000),

  (4, 'Auditoria de Segurança',
      'Revisão e reforço de segurança da plataforma antes do lançamento.',
      1701000000000, NULL,          'CANCELLED', NULL, 1, 1701000000000, 1701500000000);

-- =============================================================
-- 5. USER_PROJECT
-- =============================================================
INSERT INTO public.user_project (user_id, project_id, joined_at) VALUES
  -- Projeto 1: App Móvel
  (2, 1, 1700000000000), -- Bruno (manager)
  (4, 1, 1700050000000), -- Diogo
  (5, 1, 1700050000000), -- Eva
  (6, 1, 1700100000000), -- Filipe
  -- Projeto 2: Portal Admin
  (3, 2, 1700500000000), -- Catarina (manager)
  (4, 2, 1700550000000), -- Diogo (em dois projetos)
  (5, 2, 1700550000000), -- Eva (em dois projetos)
  -- Projeto 3: Integração API (concluído)
  (2, 3, 1699000000000),
  (6, 3, 1699000000000),
  -- Projeto 4: Auditoria (cancelado)
  (3, 4, 1701000000000);

-- =============================================================
-- 6. TASKS
-- =============================================================
INSERT INTO public.tasks (id, project_id, title, description, priority, deadline, status, created_by, created_at, updated_at) OVERRIDING SYSTEM VALUE VALUES
  -- Projeto 1 – App Móvel
  (1,  1, 'Modelação da base de dados local',
       'Definir entidades Room, relações e migrations iniciais.',
       'HIGH',     1701000000000, 'COMPLETED',  2, 1700010000000, 1701200000000),
  (2,  1, 'Implementar autenticação JWT',
       'Login, registo e refresh token com armazenamento seguro.',
       'CRITICAL', 1702000000000, 'IN_PROGRESS',2, 1700020000000, 1703000000000),
  (3,  1, 'Ecrã de listagem de tarefas',
       'UI com filtros por estado, prioridade e prazo.',
       'HIGH',     1703000000000, 'IN_PROGRESS',2, 1700030000000, 1703100000000),
  (4,  1, 'Notificações push',
       'Integração Firebase Cloud Messaging para alertas de prazo.',
       'MEDIUM',   1705000000000, 'PENDING',    2, 1700040000000, 1700040000000),
  (5,  1, 'Testes de regressão UI',
       'Suite de testes Espresso para fluxos críticos.',
       'LOW',      1707000000000, 'PENDING',    2, 1700050000000, 1700050000000),

  -- Projeto 2 – Portal Admin
  (6,  2, 'Gestão de utilizadores',
       'CRUD completo de utilizadores com paginação e pesquisa.',
       'HIGH',     1703500000000, 'IN_PROGRESS',3, 1700510000000, 1703600000000),
  (7,  2, 'Dashboard de métricas',
       'Gráficos de tarefas por estado, projeto e utilizador.',
       'MEDIUM',   1705000000000, 'PENDING',    3, 1700520000000, 1700520000000),
  (8,  2, 'Exportação de relatórios PDF',
       'Geração e download de relatórios de avaliação por projeto.',
       'LOW',      1708000000000, 'PENDING',    3, 1700530000000, 1700530000000),

  -- Projeto 3 – Integração API (concluído)
  (9,  3, 'Endpoints de autenticação',
       'POST /auth/login, /auth/refresh, /auth/logout.',
       'CRITICAL', 1700500000000, 'COMPLETED',  2, 1699010000000, 1700600000000),
  (10, 3, 'Endpoint de sincronização',
       'POST /sync com suporte a batch e resolução de conflitos.',
       'HIGH',     1701000000000, 'COMPLETED',  2, 1699020000000, 1701100000000);

-- =============================================================
-- 7. USER_TASK
-- =============================================================
INSERT INTO public.user_task (user_id, task_id, work_date, location, completion_percentage, time_spent_minutes, is_completed, updated_at) VALUES
  -- Tarefa 1 (concluída): Diogo e Eva
  (4, 1,  1700900000000, 'Escritório Lisboa',    100, 480, TRUE,  1701200000000),
  (5, 1,  1700900000000, 'Escritório Lisboa',    100, 360, TRUE,  1701200000000),
  -- Tarefa 2 (in progress): Diogo e Filipe
  (4, 2,  1703000000000, 'Remoto',                60, 300, FALSE, 1703000000000),
  (6, 2,  1703000000000, 'Escritório Porto',      40, 240, FALSE, 1703000000000),
  -- Tarefa 3 (in progress): Eva e Filipe
  (5, 3,  1703100000000, 'Remoto',                75, 420, FALSE, 1703100000000),
  (6, 3,  1703100000000, 'Escritório Porto',      50, 300, FALSE, 1703100000000),
  -- Tarefa 4 (pending): Diogo
  (4, 4,  NULL,           NULL,                    0,   0, FALSE, 1700040000000),
  -- Tarefa 6 (in progress): Diogo e Eva
  (4, 6,  1703600000000, 'Escritório Lisboa',     55, 360, FALSE, 1703600000000),
  (5, 6,  1703600000000, 'Remoto',                45, 240, FALSE, 1703600000000),
  -- Tarefa 9 (concluída): Bruno e Filipe
  (2, 9,  1700500000000, 'Escritório Lisboa',    100, 600, TRUE,  1700600000000),
  (6, 9,  1700500000000, 'Escritório Lisboa',    100, 480, TRUE,  1700600000000),
  -- Tarefa 10 (concluída): Bruno e Filipe
  (2, 10, 1701000000000, 'Escritório Lisboa',    100, 900, TRUE,  1701100000000),
  (6, 10, 1701050000000, 'Escritório Porto',     100, 720, TRUE,  1701100000000);

-- =============================================================
-- 8. OBSERVATIONS
-- =============================================================
INSERT INTO public.observations (id, task_id, user_id, text, photo_path, created_at) OVERRIDING SYSTEM VALUE VALUES
  (1, 1, 4, 'Diagrama ER revisto com o Bruno. Adicionadas tabelas sync_queue e audit_log.', NULL,                         1700950000000),
  (2, 1, 5, 'Migrations geradas e testadas em emulador Android 14.',                        NULL,                         1701000000000),
  (3, 2, 4, 'JWT implementado. Refresh token a falhar em edge case — a investigar.',         NULL,                         1703050000000),
  (4, 2, 6, 'Detetado problema de encoding no token em dispositivos Android 8.',             'photos/obs_4_screenshot.jpg',1703080000000),
  (5, 3, 5, 'Filtros por estado e prioridade funcionais. Falta o filtro por prazo.',         NULL,                         1703110000000),
  (6, 6, 4, 'CRUD de utilizadores implementado. Paginação com cursor a funcionar.',         NULL,                         1703620000000),
  (7, 9, 2, 'Todos os endpoints de auth em produção e documentados no Swagger.',             NULL,                         1700580000000),
  (8,10, 2, 'Batch sync testado com 500 registos sem degradação de performance.',            'photos/obs_8_results.jpg',   1701080000000);

-- =============================================================
-- 9. EVALUATIONS
-- =============================================================
INSERT INTO public.evaluations (id, project_id, evaluator_id, evaluated_user_id, rating, comment, created_at) OVERRIDING SYSTEM VALUE VALUES
  -- Projeto 3 concluído: Bruno avalia Filipe
  (1, 3, 2, 6, 5, 'Excelente desempenho. Entregou todas as tarefas dentro do prazo com qualidade acima do esperado.', 1702000000000),
  -- Projeto 3 concluído: Bruno avalia-se a si próprio (como USER)
  (2, 3, 2, 2, 4, 'Boa coordenação técnica. Alguma demora na resolução de blockers externos.', 1702000000000);

-- =============================================================
-- 10. AUDIT_LOG
-- =============================================================
INSERT INTO public.audit_log (id, user_id, action, entity_type, entity_id, details, timestamp) OVERRIDING SYSTEM VALUE VALUES
  (1, 1,    'CREATE', 'PROJECT',  1, '{"name":"App Móvel TaskFlow"}',                        1700000000000),
  (2, 1,    'CREATE', 'PROJECT',  2, '{"name":"Portal de Administração"}',                   1700500000000),
  (3, 1,    'CREATE', 'PROJECT',  3, '{"name":"Integração API Remota"}',                     1699000000000),
  (4, 2,    'LOGIN',   NULL,     NULL, '{"ip":"192.168.1.10","device":"Android 14"}',         1700010000000),
  (5, 4,    'LOGIN',   NULL,     NULL, '{"ip":"192.168.1.22","device":"Android 13"}',         1700020000000),
  (6, 2,    'CREATE', 'TASK',     1, '{"title":"Modelação da base de dados local"}',          1700010000000),
  (7, 2,    'UPDATE', 'TASK',     1, '{"status":{"from":"PENDING","to":"COMPLETED"}}',        1701200000000),
  (8, NULL, 'SYNC',  'TASK',     NULL,'{"synced_records":15,"errors":0}',                     1701300000000),
  (9, 1,    'UPDATE', 'PROJECT',  4, '{"status":{"from":"ACTIVE","to":"CANCELLED"}}',         1701500000000),
  (10,2,    'CREATE', 'EVALUATION',1,'{"project_id":3,"evaluated_user_id":6,"rating":5}',     1702000000000);

-- =============================================================
-- 11. SYNC_QUEUE
-- =============================================================
INSERT INTO public.sync_queue (id, endpoint, http_method, payload, created_at, retry_count, last_error) OVERRIDING SYSTEM VALUE VALUES
  (1, '/tasks/2/progress', 'PUT',
   '{"user_id":4,"completion_percentage":60,"time_spent_minutes":300}',
   1703000000000, 0, NULL),

  (2, '/tasks/3/observations', 'POST',
   '{"user_id":5,"text":"Filtros por estado e prioridade funcionais."}',
   1703110000000, 1, 'Connection timeout after 30s'),

  (3, '/tasks/6/progress', 'PUT',
   '{"user_id":4,"completion_percentage":55,"time_spent_minutes":360}',
   1703600000000, 0, NULL);

-- =============================================================
-- Verificação rápida (opcional — pode remover antes de correr)
-- =============================================================
-- SELECT 'users' AS tabela,       COUNT(*) FROM users
-- UNION ALL SELECT 'roles',       COUNT(*) FROM roles
-- UNION ALL SELECT 'user_roles',  COUNT(*) FROM user_roles
-- UNION ALL SELECT 'projects',    COUNT(*) FROM projects
-- UNION ALL SELECT 'tasks',       COUNT(*) FROM tasks
-- UNION ALL SELECT 'user_task',   COUNT(*) FROM user_task
-- UNION ALL SELECT 'observations',COUNT(*) FROM observations
-- UNION ALL SELECT 'evaluations', COUNT(*) FROM evaluations
-- UNION ALL SELECT 'audit_log',   COUNT(*) FROM audit_log
-- UNION ALL SELECT 'sync_queue',  COUNT(*) FROM sync_queue;

-- =============================================================
-- 12. ALINHAR SEQUENCES
-- =============================================================
-- Como a seed usa IDs explicitos, e necessario reposicionar as
-- sequences para que os proximos inserts automaticos nao colidam.
select setval(pg_get_serial_sequence('public.roles', 'id'), coalesce((select max(id) from public.roles), 1), true);
select setval(pg_get_serial_sequence('public.users', 'id'), coalesce((select max(id) from public.users), 1), true);
select setval(pg_get_serial_sequence('public.projects', 'id'), coalesce((select max(id) from public.projects), 1), true);
select setval(pg_get_serial_sequence('public.tasks', 'id'), coalesce((select max(id) from public.tasks), 1), true);
select setval(pg_get_serial_sequence('public.observations', 'id'), coalesce((select max(id) from public.observations), 1), true);
select setval(pg_get_serial_sequence('public.evaluations', 'id'), coalesce((select max(id) from public.evaluations), 1), true);
select setval(pg_get_serial_sequence('public.audit_log', 'id'), coalesce((select max(id) from public.audit_log), 1), true);
select setval(pg_get_serial_sequence('public.sync_queue', 'id'), coalesce((select max(id) from public.sync_queue), 1), true);
select setval(pg_get_serial_sequence('public.device_tokens', 'id'), coalesce((select max(id) from public.device_tokens), 1), true);
