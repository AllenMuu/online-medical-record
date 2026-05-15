# Repository Guidelines

## Project Structure & Module Organization

This is a full-stack clinical records prototype. The Spring Boot Java 21 backend lives in `backend/`; application code is under `backend/src/main/java/com/ice/medicalrecord/`, grouped by `api`, `domain`, `repository`, `security`, and `config`, with tests in `backend/src/test/java/`. The Vite React TypeScript frontend lives in `frontend/`; source is in `frontend/src/`, shared UI in `frontend/src/components/`, route pages in `frontend/src/pages/`, unit tests beside source files, and Playwright tests in `frontend/e2e/`.

## Build, Test, and Development Commands

- `docker compose up -d db`: start the local PostgreSQL database.
- `cd backend && mvn spring-boot:run`: run the API at `http://localhost:8080/api`.
- `cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=demo`: run with the in-memory demo profile.
- `cd backend && mvn test`: run backend JUnit/Spring integration tests.
- `cd frontend && npm ci`: install frontend dependencies from `package-lock.json`.
- `cd frontend && npm run dev`: start Vite on `http://localhost:5173`.
- `cd frontend && npm run build`: typecheck and build the frontend.
- `cd frontend && npm run lint`: run ESLint.
- `cd frontend && npm test`: run Vitest unit tests.
- `cd frontend && npm run test:e2e`: run Playwright; set `E2E_BASE_URL` for non-default Vite ports.

## Coding Style & Naming Conventions

Use 4-space indentation for Java and 2-space indentation for TypeScript/TSX. Keep Java packages aligned with `com.ice.medicalrecord` and name Spring components by role, such as `PatientRepository` or `SecurityConfig`. Use PascalCase for React components/pages, camelCase for functions and variables, and `*.test.ts` or `*.e2e.ts` for tests. Prefer existing DTO, repository, page, and component patterns before adding abstractions.

## Testing Guidelines

Backend tests use JUnit 5, Spring Boot test support, and MockMvc. Add integration coverage for API, auth, and persistence changes. Frontend unit tests use Vitest near the code they cover. Playwright covers clinical flows in `frontend/e2e/`; run it after login, routing, form, or API integration changes.

## Commit & Pull Request Guidelines

Current history is minimal, so use concise imperative subjects and add body context for behavior changes. Include Lore-style trailers when useful, for example `Tested: cd backend && mvn test` and `Not-tested: Playwright not run`. Pull requests should describe the change, list verification, link issues, and include screenshots for visible UI changes.

## Security & Configuration Tips

Do not commit real patient data, production secrets, or local credentials. Demo accounts in `README.md` are for local development only. Keep environment-specific settings in Spring profiles or local environment variables, such as `VITE_API_PROXY_TARGET`.

## Agent-Specific Instructions

Always respond to repository contributors in Chinese.
