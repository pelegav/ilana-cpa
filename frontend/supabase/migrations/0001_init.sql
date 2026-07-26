-- Initial schema: profiles (roles/status), documents, document_notes, RLS, storage bucket.

create extension if not exists "pgcrypto";

-- ---------------------------------------------------------------------------
-- profiles
-- ---------------------------------------------------------------------------

create table public.profiles (
  id uuid primary key references auth.users (id) on delete cascade,
  email text not null,
  full_name text,
  role text not null default 'client' check (role in ('client', 'admin')),
  status text not null default 'active' check (status in ('active', 'invited', 'disabled')),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

alter table public.profiles enable row level security;

-- Auto-create a profile row whenever a new auth user is created.
create function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, email)
  values (new.id, new.email);
  return new;
end;
$$;

create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

-- Helper used throughout RLS policies below.
create function public.is_admin()
returns boolean
language sql
security definer
set search_path = public
stable
as $$
  select exists (
    select 1 from public.profiles
    where id = auth.uid() and role = 'admin'
  );
$$;

-- Clients can view/update their own profile; admins can view/update all.
-- `role` and `status` are excluded from the client self-update policy.
create policy "profiles_select_own_or_admin"
  on public.profiles for select
  using (id = auth.uid() or public.is_admin());

create policy "profiles_update_own"
  on public.profiles for update
  using (id = auth.uid())
  with check (id = auth.uid() and role = 'client' and status = 'active');

create policy "profiles_update_admin"
  on public.profiles for update
  using (public.is_admin());

create policy "profiles_insert_admin"
  on public.profiles for insert
  with check (public.is_admin());

-- ---------------------------------------------------------------------------
-- documents
-- ---------------------------------------------------------------------------

create table public.documents (
  id uuid primary key default gen_random_uuid(),
  owner_id uuid not null references public.profiles (id) on delete cascade,
  storage_path text not null,
  file_name text not null,
  category text,
  status text not null default 'pending' check (status in ('pending', 'reviewed', 'needs_info')),
  uploaded_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index documents_owner_id_idx on public.documents (owner_id);

alter table public.documents enable row level security;

create policy "documents_select_own_or_admin"
  on public.documents for select
  using (owner_id = auth.uid() or public.is_admin());

create policy "documents_insert_own"
  on public.documents for insert
  with check (owner_id = auth.uid());

create policy "documents_update_admin"
  on public.documents for update
  using (public.is_admin());

create policy "documents_delete_admin"
  on public.documents for delete
  using (public.is_admin());

-- ---------------------------------------------------------------------------
-- document_notes
-- ---------------------------------------------------------------------------

create table public.document_notes (
  id uuid primary key default gen_random_uuid(),
  document_id uuid not null references public.documents (id) on delete cascade,
  author_id uuid not null references public.profiles (id),
  body text not null,
  created_at timestamptz not null default now()
);

create index document_notes_document_id_idx on public.document_notes (document_id);

alter table public.document_notes enable row level security;

create policy "document_notes_select_own_or_admin"
  on public.document_notes for select
  using (
    public.is_admin()
    or exists (
      select 1 from public.documents
      where documents.id = document_notes.document_id
        and documents.owner_id = auth.uid()
    )
  );

create policy "document_notes_insert_admin"
  on public.document_notes for insert
  with check (public.is_admin() and author_id = auth.uid());

-- ---------------------------------------------------------------------------
-- storage: client-documents bucket
-- ---------------------------------------------------------------------------

insert into storage.buckets (id, name, public)
values ('client-documents', 'client-documents', false);

-- Path convention: {owner_id}/{document_id}/{file_name} — first path segment
-- must match the uploader's auth.uid() for client access; admins get full access.

create policy "storage_client_documents_select"
  on storage.objects for select
  using (
    bucket_id = 'client-documents'
    and (
      (storage.foldername(name))[1] = auth.uid()::text
      or public.is_admin()
    )
  );

create policy "storage_client_documents_insert"
  on storage.objects for insert
  with check (
    bucket_id = 'client-documents'
    and (storage.foldername(name))[1] = auth.uid()::text
  );

create policy "storage_client_documents_admin_manage"
  on storage.objects for all
  using (bucket_id = 'client-documents' and public.is_admin())
  with check (bucket_id = 'client-documents' and public.is_admin());
