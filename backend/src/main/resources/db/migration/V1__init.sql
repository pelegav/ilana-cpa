-- Initial schema for the Spring Boot backend: users, refresh_tokens, documents, audit_log.
-- Supabase Postgres is used purely as a managed database here — no Supabase Auth, no RLS.
-- Authorization is enforced entirely in the application layer (Spring Security).

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- users
-- ---------------------------------------------------------------------------

create table users (
  id uuid primary key default gen_random_uuid(),
  email text not null unique,
  password_hash text not null,
  full_name text,
  role text not null default 'CLIENT' check (role in ('ADMIN', 'ACCOUNTANT', 'CLIENT')),
  status text not null default 'active' check (status in ('active', 'invited', 'disabled')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

-- ---------------------------------------------------------------------------
-- refresh_tokens
-- ---------------------------------------------------------------------------

create table refresh_tokens (
  id uuid primary key default gen_random_uuid(),
  user_id uuid not null references users (id) on delete cascade,
  token_hash text not null,
  family_id uuid not null,
  expires_at timestamptz not null,
  revoked_at timestamptz,
  replaced_by_token_id uuid,
  created_at timestamptz not null default now()
);

create index refresh_tokens_token_hash_idx on refresh_tokens (token_hash);
create index refresh_tokens_user_id_idx on refresh_tokens (user_id);
create index refresh_tokens_family_id_idx on refresh_tokens (family_id);

-- ---------------------------------------------------------------------------
-- documents
-- ---------------------------------------------------------------------------

create table documents (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references users (id) on delete restrict,
  storage_path text not null,
  file_name text not null,
  category text,
  status text not null default 'pending' check (status in ('pending', 'reviewed', 'needs_info')),
  uploaded_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index documents_owner_id_idx on documents (owner_id);

-- ---------------------------------------------------------------------------
-- audit_log
-- ---------------------------------------------------------------------------

create table audit_log (
  id bigint generated always as identity primary key,
  actor_id uuid references users (id) on delete set null,
  action text not null,
  entity_type text,
  entity_id text,
  metadata jsonb,
  ip_address text,
  created_at timestamptz not null default now()
);

create index audit_log_entity_idx on audit_log (entity_type, entity_id);
create index audit_log_actor_id_idx on audit_log (actor_id);
