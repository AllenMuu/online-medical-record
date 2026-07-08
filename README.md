# online-medical-record

A full-stack prototype for a clinical precision system: Spring Boot API + React admin app + PostgreSQL 18.

[简体中文](./README_zh.md)

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
