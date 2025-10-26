# Repository Guidelines

## Project Structure & Module Organization
- Backend (`jeecg-boot/`): Maven multi‑module Spring Boot. Key modules: `jeecg-boot-base-core`, `jeecg-module-system` (entry app `jeecg-system-start`), `jeecg-module-energy`, `jeecg-module-demo`, optional cloud modules under `jeecg-server-cloud/`. Config lives in `jeecg-module-system/jeecg-system-start/src/main/resources/application-*.yml`.
- Frontend (`jeecgboot-vue3/`): Vue 3 + Vite app.
- Infra: root `docker-compose.yml` (MySQL, Redis, backend, Nginx); DB init under `jeecg-boot/db/`. Utility folders: `logs/`, `temp/`.

## Build, Test, and Development Commands
- Backend build: `cd jeecg-boot && mvn -T 1C clean install -DskipTests` (compile all modules fast).
- Backend dev (hot reload): `cd jeecg-boot && npm run dev` (Windows: `start-dev.bat`). Runs code watcher + `spring-boot:run` with `dev` profile.
- Backend run jar: `mvn -pl jeecg-module-system/jeecg-system-start -am package && java -jar jeecg-module-system/jeecg-system-start/target/*.jar`.
- Backend tests: `mvn test` (module only: `mvn -pl <module> test`).
- Frontend dev: `cd jeecgboot-vue3 && pnpm install && pnpm dev` (Node ^18 or ≥20).
- Full stack (Docker): from repo root `docker-compose up -d`.

## Coding Style & Naming Conventions
- Java: 4‑space indent; packages lowercase; classes `UpperCamelCase`; methods/fields `lowerCamelCase`; constants `UPPER_SNAKE_CASE`. Prefer constructor injection; use Lombok where present. REST paths lowercase and hyphenated.
- Vue/TS: Use ESLint + Prettier. Format with `pnpm batch:prettier`. Component names `PascalCase`, files `kebab-case.vue`; variables `camelCase`.

## Testing Guidelines
- Java: Place tests under `src/test/java`, name as `*Test.java`. Use JUnit/Spring Boot test utilities. Favor fast, deterministic tests; add integration tests for critical flows.
- Frontend: Jest + `@vue/test-utils` are available; place `*.spec.ts` next to components or in `tests/`.

## Commit & Pull Request Guidelines
- Commits: short imperative subject (≤72 chars), optionally prefix module (e.g., `[system] Fix login rate limit`); reference issues (`#123`). Keep changes scoped.
- PRs: include purpose, linked issues, how to test (commands/config), and screenshots for UI. Note DB or config changes. Ensure `mvn clean install` and frontend build pass locally.

## Security & Configuration Tips
- Configure local DB/Redis in `application-dev.yml`; never commit secrets. Select profile via `-Dspring.profiles.active=dev|test|prod`. Frontend env overrides go in `.env.local`.

