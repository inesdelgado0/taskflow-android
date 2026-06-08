create table if not exists users (
  id bigserial primary key,
  name text not null,
  username text not null unique,
  email text not null unique,
  password_hash text not null,
  photo_url text,
  is_active boolean not null default true,
  created_at bigint not null,
  updated_at bigint not null
);

create table if not exists roles (
  id bigserial primary key,
  code text not null unique check (code in ('ADMIN', 'MANAGER', 'USER')),
  description text,
  created_at bigint not null
);

insert into roles (code, description, created_at)
values
  ('ADMIN', 'Administrador', 0),
  ('MANAGER', 'Gestor de Projeto', 0),
  ('USER', 'Utilizador', 0)
on conflict (code) do nothing;

create table if not exists user_roles (
  user_id bigint not null references users(id) on delete cascade,
  role_id bigint not null references roles(id) on delete restrict,
  assigned_at bigint not null,
  primary key (user_id, role_id)
);

-- Seed roles (one-time insert, harmless on re-run)
insert into user_roles (user_id, role_id, assigned_at)
select users.id, roles.id, coalesce(users.created_at, 0)
from users
join roles on roles.code = 'USER'
on conflict (user_id, role_id) do nothing;

create table if not exists projects (
  id bigserial primary key,
  name text not null,
  description text,
  start_date bigint,
  end_date bigint,
  status text not null default 'ACTIVE' check (status in ('ACTIVE', 'COMPLETED', 'CANCELLED')),
  manager_id bigint references users(id) on delete set null,
  created_by bigint not null references users(id) on delete restrict,
  created_at bigint not null,
  updated_at bigint not null
);

create table if not exists tasks (
  id bigserial primary key,
  project_id bigint not null references projects(id) on delete cascade,
  title text not null,
  description text,
  priority text not null default 'MEDIUM' check (priority in ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
  deadline bigint,
  status text not null default 'PENDING' check (status in ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
  created_by bigint not null references users(id) on delete restrict,
  created_at bigint not null,
  updated_at bigint not null
);

create table if not exists user_project (
  user_id bigint not null references users(id) on delete cascade,
  project_id bigint not null references projects(id) on delete cascade,
  joined_at bigint not null,
  primary key (user_id, project_id)
);

create table if not exists user_task (
  user_id bigint not null references users(id) on delete cascade,
  task_id bigint not null references tasks(id) on delete cascade,
  work_date bigint,
  location text,
  completion_percentage integer not null default 0 check (completion_percentage between 0 and 100),
  time_spent_minutes integer not null default 0,
  is_completed boolean not null default false,
  updated_at bigint not null,
  primary key (user_id, task_id)
);

create table if not exists observations (
  id bigserial primary key,
  task_id bigint not null references tasks(id) on delete cascade,
  user_id bigint not null references users(id) on delete restrict,
  text text,
  photo_path text,
  created_at bigint not null,
  check (text is not null or photo_path is not null)
);

create table if not exists evaluations (
  id bigserial primary key,
  project_id bigint not null references projects(id) on delete cascade,
  evaluator_id bigint not null references users(id) on delete restrict,
  evaluated_user_id bigint not null references users(id) on delete restrict,
  rating integer not null check (rating between 1 and 5),
  comment text,
  created_at bigint not null,
  unique (project_id, evaluated_user_id)
);

create table if not exists audit_log (
  id bigserial primary key,
  user_id bigint references users(id) on delete set null,
  action text not null check (action in ('LOGIN', 'LOGOUT', 'CREATE', 'UPDATE', 'DELETE', 'SYNC')),
  entity_type text,
  entity_id bigint,
  details jsonb,
  timestamp bigint not null
);

create table if not exists sync_queue (
  id bigserial primary key,
  endpoint text not null,
  http_method text not null check (http_method in ('GET', 'POST', 'PUT', 'DELETE')),
  payload text,
  created_at bigint not null,
  retry_count integer not null default 0,
  last_error text
);

create table if not exists device_tokens (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  token text not null unique,
  platform text not null default 'ANDROID' check (platform in ('ANDROID', 'IOS', 'WEB')),
  device_name text,
  is_active boolean not null default true,
  created_at bigint not null,
  updated_at bigint not null,
  last_seen_at bigint not null
);

create index if not exists idx_projects_status on projects(status);
create index if not exists idx_roles_code on roles(code);
create index if not exists idx_user_roles_user on user_roles(user_id);
create index if not exists idx_user_roles_role on user_roles(role_id);
create index if not exists idx_projects_manager on projects(manager_id);
create index if not exists idx_tasks_project on tasks(project_id);
create index if not exists idx_tasks_status on tasks(status);
create index if not exists idx_tasks_priority on tasks(priority);
create index if not exists idx_tasks_deadline on tasks(deadline);
create index if not exists idx_observations_task on observations(task_id);
create index if not exists idx_evaluations_project on evaluations(project_id);
create index if not exists idx_audit_user on audit_log(user_id);
create index if not exists idx_audit_timestamp on audit_log(timestamp);
create index if not exists idx_sync_created_at on sync_queue(created_at);
create index if not exists idx_device_tokens_user on device_tokens(user_id);
create index if not exists idx_device_tokens_active on device_tokens(is_active);
