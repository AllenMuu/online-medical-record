# online-medical-record

A full-stack prototype for a clinical precision system: Spring Boot API + React admin app + PostgreSQL 18.

[简体中文](./README_zh.md)

## Project Overview

A clinical precision system prototype — a clinician-facing workstation for managing patients and medical records. It is a full-stack monorepo with a Spring Boot API, a React admin frontend, and PostgreSQL.

### Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot 4.0.6, Java 21, Spring Data JPA, Spring Security, Bean Validation |
| Frontend | React 19, TypeScript 5.9, Vite 7, React Router 7, Tailwind CSS 3 |
| Database | PostgreSQL 18 (H2 in-memory for the `demo` profile) |
| Testing | JUnit 5 / MockMvc (backend), Vitest + Playwright (frontend) |
| Infra | Docker Compose (db + backend + frontend) |

### Core Features

- **Authentication & authorization** — session-based auth over HTTP-only cookies, BCrypt password hashing, role-based access (`ADMIN` / `DOCTOR`), self-service password reset, and avatar upload.
- **Patient management** — CRUD with paginated search by name and team.
- **Medical records** — structured visit records (complaint, examination, diagnosis, treatment, prognosis, notes) with a one-to-many medication list and a `COMPLETED` / `IN_PROGRESS` status; filterable by patient and doctor.
- **Doctor account management** — admins create and manage doctor accounts, each with a profile (department, avatar).
- **Dashboard** — operational summary (patient count, record count, new-this-month, doctor count).
- **Audit logging** — JPA auditing tracks created/updated metadata for core entities.

### Architecture

The backend follows a layered package structure under `com.ice.medicalrecord`:

- `api/` — REST controllers and DTOs
- `domain/` — JPA entities and enums
- `repository/` — Spring Data JPA repositories
- `service/` — business logic and mappers
- `security/` — Spring Security configuration and user details
- `config/` — auditing, static resources, and data seeding

The frontend (`frontend/src/`) groups route pages in `pages/`, reusable UI in `components/`, and shared utilities/auth context at the source root. See [`docs/DESIGN.md`](./docs/DESIGN.md) and [`docs/backend-api.md`](./docs/backend-api.md) for the design and API references.

## Quick Start

### Deploy the entire project with Docker in one command

```bash
./start-docker.sh
```

Once started, visit:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`

If you want to follow the logs right after startup:

```bash
./start-docker.sh --logs
```

Stop and remove the containers:

```bash
./stop-docker.sh
```

This only stops and removes the containers; it does not delete the database data volume.

If you explicitly want to wipe the local database data, run separately:

```bash
docker compose down -v
```

### Run locally for development

```bash
docker compose up -d db
cd backend && mvn spring-boot:run
cd ../frontend && npm ci && npm run dev
```

You can also start both the frontend and backend in one go from the repository root:

```bash
chmod +x ./start-local.sh
./start-local.sh
```

By default it tries the full Docker environment first; if Docker is unavailable it falls back to `demo` mode (H2 in-memory database). To force a connection to the local Docker PostgreSQL:

```bash
./start-local.sh docker
```

If you just want a quick local trial, you can skip Docker entirely and use the in-memory database demo profile:

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo
cd ../frontend && npm ci && npm run dev
```

If port `8080` is already taken, you can switch to another backend port and have the Vite proxy follow:

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo -Dspring-boot.run.arguments=--server.port=8081
cd ../frontend && VITE_API_PROXY_TARGET=http://localhost:8081 npm run dev
```

## PostgreSQL (Docker)

The `docker-compose.yml` bundled with this repository uses `postgres:18.4-alpine` and starts the following by default:

- Container name: `aura-medical-postgres`
- Database: `medical_record`
- Username: `medical`
- Password: `medical`
- Port: `5432`

To comply with the data directory rules of the official PostgreSQL 18 image, the current configuration uses:

- `PGDATA=/var/lib/postgresql/18/docker`
- Docker volume: `postgres18_data`

This is a new project with no local historical database to migrate. If you need a clean local database, you can simply delete and recreate the corresponding volume.

Default accounts:

- Administrator: `admin@aura.local` / `Admin123!`
- Doctor: `doctor@aura.local` / `Doctor123!`

Frontend dev URL: `http://localhost:5173`
Backend API: `http://localhost:8080/api`

## Contributing

Contributions are welcome! This is an open-source project under the Apache 2.0 license.

### Ways to contribute

- Report bugs or request features via GitHub Issues
- Open a Pull Request with a fix or improvement
- Improve documentation in `docs/`

### Development workflow

1. Fork the repository and clone your fork.
2. Set up the dev environment following [Quick Start](#quick-start) - the `demo` profile (H2 in-memory DB) is the fastest path: `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo`.
3. Create a feature branch from `main`.
4. Make your changes, keeping code style and tests aligned with [`AGENTS.md`](./AGENTS.md).
5. Run the relevant tests:
   - Backend: `cd backend && mvn test`
   - Frontend unit: `cd frontend && npm test`
   - Frontend e2e: `cd frontend && npm run test:e2e`
6. Commit with [Conventional Commits](https://www.conventionalcommits.org/) messages (e.g. `feat(records): ...`, `fix(auth): ...`).
7. Push and open a Pull Request describing the change and how you verified it.

### Guidelines

- Follow the coding style, naming, and testing conventions in [`AGENTS.md`](./AGENTS.md).
- Add or update tests for behavior changes; aim for meaningful coverage of API, auth, and persistence.
- Do **not** commit real patient data, production secrets, or local credentials - demo accounts are for local development only.
- Keep PRs focused; include screenshots for visible UI changes.

## License

Licensed under the [Apache License 2.0](./LICENSE).
