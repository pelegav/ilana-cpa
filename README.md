# Ilana CPA

A client portal for an accounting practice: a Next.js front door and a Spring Boot
backend, backed by Supabase's managed Postgres. This README is the operations
reference — how to start it, log in, and what each role can do.

**Status:** Frontend live on Vercel. Backend runs locally / in local Docker only —
not deployed anywhere public yet. Custom domain not connected yet.

## Quick start

Two processes, two terminals. Backend first.

```bash
# backend/ — starts the API on localhost:8082, wired to Supabase Postgres
docker compose up -d
```

```bash
# frontend/ — starts the site on localhost:3000
npm run dev
```

Log in at `localhost:3000/login` with the admin account — email
`ilana@ir-cpa.co.il`, password is the `ADMIN_PASSWORD` value in `backend/.env`
(not repeated here since this file is committed to git). There's no
password-reset screen yet.

## How it fits together

| Piece | Role |
|---|---|
| `frontend/` | Next.js 16. Marketing pages, login, and the client/admin dashboards. Talks to the backend server-side only — the browser never calls Spring Boot directly. |
| `backend/` | Spring Boot 4.1 (Java 21). Owns authentication, authorization, documents, and audit logging. The only thing that touches the database or Supabase Storage. |
| Supabase | Managed Postgres for data, plus Storage for uploaded files. No Supabase Auth, no Row Level Security — that's deliberate; the backend owns all of that now. |

Roles: `ADMIN` and `ACCOUNTANT` share the staff area today. `CLIENT` only sees
their own documents.

## Running it in full

1. **Start the backend.** From `backend/`, either `docker compose up -d`
   (recommended — matches what's actually verified), or without Docker: source
   `backend/.env` and run `mvn spring-boot:run`. First boot takes ~30–90s while
   it validates the Flyway migration and connects through Supabase's pooler.
2. **Confirm it's healthy.** Visit `localhost:8082/actuator/health` — should
   return `{"status":"UP"}` with no login prompt.
3. **Start the frontend.** From `frontend/`, run `npm run dev`. It reads
   `BACKEND_API_URL` from `.env.local` — already pointed at `localhost:8082`.
4. **Stop everything.** `Ctrl+C` the frontend terminal, then
   `docker compose down` from `backend/`.

> **Both env files hold real secrets** (DB password, JWT signing key, Supabase
> service-role key). They're gitignored — never commit `backend/.env` or
> `frontend/.env.local`.

## Using it as admin

Log in, land on `/admin`.

- **Create a client account** — fill in name + email, submit. A temporary
  password appears once; copy it and send it to the client yourself (no email
  delivery yet). They use it to log in at `/login`.
- **Review uploaded documents** — the API supports listing every client's
  documents and changing status (`pending` → `reviewed` / `needs_info`), but
  that review screen isn't built yet. Only the client-facing upload/download UI
  exists so far.

## Using it as a client

Log in with the credentials admin gave you, land on `/portal`.

1. **Upload** — choose a file, optionally tag it with a category (e.g. "tax
   return"), submit. 25MB limit.
2. **Track status** — each document shows its review status. Only staff can
   change it.
3. **Download** — the button requests a signed link, valid 5 minutes, then
   opens it in a new tab. You'll never see another client's files — that's
   enforced on the backend, not just hidden in the UI.

## Sessions & security

| Behavior | Detail |
|---|---|
| Login session | 15-minute access token, refreshed silently in the background for up to 30 days |
| Reused old token | Treated as possible theft — the whole session family is revoked, forcing a fresh login |
| Admin disables a client | Their session dies within one refresh cycle; they can't log back in |
| Failed logins | Rate-limited per account and per IP — expect a short lockout after repeated wrong passwords |
| Every access | Logged to an audit trail: logins, uploads, downloads, status changes |

## Deployment status

- **Frontend** — live at [ilana-cpa.vercel.app](https://ilana-cpa.vercel.app),
  deployed manually via Vercel CLI (GitHub auto-deploy isn't connected yet).
- **Backend** — not deployed anywhere public — runs locally or in local Docker
  only. Hosting choice (Railway, Fly.io, etc.) is still open.
- **Database** — Supabase Postgres, reached through the Session Pooler (the
  direct connection is IPv6-only and unreliable from this network).
- **Domain** — not yet connected — site is Vercel-subdomain only.

## Where things live

```
frontend/
  src/app/(marketing)/       public site
  src/app/portal/            client area
  src/app/admin/             staff area
  src/proxy.ts                route gating, session refresh
  src/lib/api/client.ts       calls to the backend

backend/
  src/main/java/.../auth/      login, tokens, refresh
  src/main/java/.../document/  upload, download, status
  src/main/java/.../user/      accounts, roles
  src/main/java/.../audit/     activity log
  src/main/resources/db/migration/   schema (Flyway)
  Dockerfile, docker-compose.yml
```

## Troubleshooting

- **Backend won't start / port 8082 busy** — something else is already
  listening. Stop it, or change `PORT` in `backend/.env` and the compose
  file's port mapping.
- **Backend starts but can't reach the database** — check `DB_URL` in
  `backend/.env` uses the Session Pooler host
  (`aws-1-ap-northeast-2.pooler.supabase.com`), not the direct-connection
  host — that one is IPv6-only and has been unreliable.
- **Frontend shows "Failed to fetch" on login** — the backend isn't running,
  or `BACKEND_API_URL` in `frontend/.env.local` doesn't match its port.
- **Locked out after a few wrong passwords** — expected, rate limiting. Wait a
  minute for IP limits, up to 15 for the per-account limit.

---

Foundation verified: 26/26 API checks, 11/11 browser checks against the
Dockerized backend.
