# Conventions

## Branching

- Branch naming: `feature/DEV-<n>` where `<n>` matches the Jira ticket number.
- All feature work branches off `develop`.

## Commits

- Short, descriptive messages. No enforced format (no conventional commits).
- Example: `DEV-19: fix endpoint mismatch; add exception handling`

## Pull Requests

- PR title must reference the Jira ticket: `DEV-<n>: <short description>` (e.g. `DEV-23: Google OAuth UI`).
- After pushing, post an update in the **#arendus** Discord channel.
- One approval is sufficient to merge. Either the reviewer or the author may merge.
- Target branch: `develop`.

## Environments & Deployment

- `develop` branch auto-deploys to the development environment on every merge via GitHub Actions.
- `main` branch exists but has no automated deployment yet.

## Testing

<!-- TODO: Testing strategy will be documented here once finalized. -->

## API Conventions

- Base path: `/api/v1/` — all endpoints must be versioned consistently.
- RESTful, resource-oriented design.
- Separate request/response DTOs — never expose JPA entities directly.
- Error responses follow the standard format defined in [ARCHITECTURE.md](ARCHITECTURE.md).
- Document endpoints with `springdoc-openapi` annotations (`@Tag`, `@Operation`, `@ApiResponse`).

## Backend (Java / Spring Boot)

### Structure

Layered architecture under `com.app.backend`:

| Package | Contains |
|---------|----------|
| `controller` | REST controllers (`*Controller`) |
| `service` | Business logic (`*Service`) |
| `repository` | Spring Data JPA interfaces (`*Repository`) |
| `model` | JPA entities and `enums/` |
| `dto/request` | Incoming request bodies |
| `dto/response` | Outgoing response bodies |
| `config` | Spring configuration classes |
| `security` | JWT filter, token service |
| `exception` | Custom exceptions, global error handler |

### Style

- Use Lombok: `@RequiredArgsConstructor` for constructor injection, `@Slf4j` where logging is needed.
- Mark read-only service methods with `@Transactional(readOnly = true)`.
- Add Javadoc to public methods on controllers and services.
- Import order: `com.app` → third-party libraries → `org.springframework` → `java.*`.

## Frontend (Angular)

### Structure

Components and pages live under `frontend/src/app/`:

| Folder | Contains |
|--------|----------|
| `pages/` | Routed page components (lazy-loaded via `loadComponent`) |
| `components/` | Reusable UI components |
| `services/` | Singleton services |
| `guards/` | Route guards |
| `interceptors/` | HTTP interceptors |

### Style

- All components are **standalone** (`standalone: true`).
- Selector prefix: `app-` (e.g. `app-header`, `app-quiz`).
- File naming: kebab-case (`hero-section.component.ts`).
- Styling: **SCSS**, scoped per component.
- Use Angular **signals** for component/service state; use RxJS observables for HTTP and async streams.
- Import order: `@angular/*` → third-party (`rxjs`, etc.) → relative project imports.

## Database Migrations

- Managed by Flyway in `backend/src/main/resources/db/migration/`.
- Naming: `V<version>__<description>.sql` (e.g. `V1__initial_schema.sql`).
- Migrations are append-only — never edit a migration that has already run.
