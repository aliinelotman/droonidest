# Architecture

> System design, service boundaries, data flow

## Overview

Droonidest is a drone-related interactive online course platform. The first module is publicly accessible (ABC of drone defence with AI voiceover and SVG animations). The second module requires authentication and contains interactive exercises (e.g. drag-and-drop FPV drone assembly). Administrators manage course content through an in-app editor.

## Tech Stack

| Layer | Technology | Purpose |
|-------|-----------|---------|
| Frontend | Angular 19 (standalone, SCSS) | SPA with lazy-loaded feature modules |
| Backend | Spring Boot 3.4 / Java 21 | REST API, business logic, auth |
| Database | PostgreSQL | Persistent storage |
| Migrations | Flyway | Schema versioning |
| File Storage | MinIO (S3-compatible) | Audio files, images, SVG assets |
| TTS | Google Cloud Text-to-Speech | AI voiceover generation at build time |
| Auth | Google OAuth 2.0 + JWT | Stateless authentication |
| Containerization | Docker + Docker Compose | Local dev, deployment |

### Stack decisions (MVP)

- **No Redis** — JWT-based stateless auth eliminates the need for session storage. Revisit if caching becomes necessary.
- **No RabbitMQ** — Spring `@Async` handles background tasks (e.g. TTS generation). Revisit if we need reliable retries or multiple consumers.
- **MinIO over S3** — Runs locally in Docker, same API as AWS S3. When migrating to AWS, swap the endpoint config with zero code changes.

## System Architecture

```mermaid
graph LR
    subgraph Client
        A[Angular SPA<br/>Nginx]
    end

    subgraph Backend
        B[Spring Boot<br/>API Server]
    end

    subgraph Data
        C[(PostgreSQL)]
        D[(MinIO<br/>S3-compatible)]
    end

    subgraph External
        E[Google Cloud<br/>TTS API]
    end

    A -- "REST / JSON" --> B
    B -- "JDBC" --> C
    B -- "S3 API" --> D
    B -. "@Async" .-> E
```

## Environments

| Environment | Purpose | Infrastructure |
|-------------|---------|---------------|
| `local` | Developer workstation | Docker Compose (all services) |
| `development` | Shared testing / staging | Docker Compose on VPS |
| `production` | Live site | Docker on VPS (→ AWS later) |

Configuration is managed via Spring profiles (`application-local.yml`, `application-dev.yml`, `application-prod.yml`) and Angular environment files.

## Authentication & Authorization

- **Provider**: Google OAuth 2.0 (via Spring Security OAuth2 Client)
- **Token strategy**: Stateless JWT issued by the backend after Google login
  - Access token: short-lived (15 min), sent in `Authorization: Bearer` header
  - Refresh token: longer-lived (7 days), stored in `httpOnly` cookie
- **Roles**: `USER`, `ADMIN` — stored in the `users` table, embedded in JWT claims
- **Public routes**: Module 1 content, landing page — no auth required
- **Protected routes**: Module 2+, progress tracking, admin panel — JWT required

### Auth Flow

```mermaid
sequenceDiagram
    actor User
    participant Angular
    participant Backend
    participant Google

    User->>Angular: Click "Sign in with Google"
    Angular->>Google: Redirect to Google OAuth consent
    Google->>Angular: Redirect back with authorization code
    Angular->>Backend: POST /api/v1/auth/google {code}
    Backend->>Google: Exchange code for Google tokens
    Google-->>Backend: Google access token + user info
    Backend->>Backend: Find or create user in DB
    Backend->>Backend: Generate JWT (access + refresh)
    Backend-->>Angular: Access token in body,<br/>refresh token in httpOnly cookie
    Angular->>Angular: Store access token in memory

    Note over Angular,Backend: Subsequent API calls

    Angular->>Backend: GET /api/v1/progress<br/>Authorization: Bearer {jwt}
    Backend->>Backend: Validate JWT, extract roles
    Backend-->>Angular: 200 OK + data

    Note over Angular,Backend: Token refresh

    Angular->>Backend: POST /api/v1/auth/refresh<br/>(httpOnly cookie sent automatically)
    Backend->>Backend: Validate refresh token
    Backend-->>Angular: New access token
```

## Backend Structure

Single Spring Boot application with a **layered architecture** — code is organized by technical responsibility rather than by feature:

```
backend/src/main/java/com/app/backend/
├── config/             # Spring configuration (security, S3, async, CORS)
├── security/           # JWT filter, OAuth2 handlers, token service
├── controller/         # REST controllers (presentation layer)
├── service/            # Business logic (service layer)
├── repository/         # Spring Data JPA repositories (data access layer)
├── model/              # JPA entities
│   └── enums/          # Enums (UserRole, QuestionType, LessonStatus)
├── dto/                # Data Transfer Objects
│   ├── request/        # Incoming request bodies
│   └── response/       # Outgoing response bodies
└── exception/          # Custom exceptions, global error handler
```

### Layer rules

| Layer | Depends on | Never depends on |
|-------|-----------|-----------------|
| `controller` | `service`, `dto` | `repository`, `model` (except enums) |
| `service` | `repository`, `model`, `dto` | `controller` |
| `repository` | `model` | `controller`, `service` |
| `dto` | `model/enums` | `repository`, `service`, `controller` |
| `exception` | — | anything except Spring framework |

## Frontend Structure

Angular SPA with lazy-loaded feature modules:

```
frontend/src/app/
├── core/              # Singleton services, interceptors, guards
├── shared/            # Reusable components, pipes, directives
├── features/
│   ├── public-course/ # Module 1 (public), voiceover player, SVG animations
│   ├── auth/          # Login flow, Google OAuth callback
│   ├── dashboard/     # User dashboard, progress overview
│   └── admin/         # Content editor, module management
└── app.routes.ts      # Top-level routing with lazy loading
```

## API Design

- **Base path**: `/api/v1/`
- **Style**: RESTful, resource-oriented
- **Pagination**: Spring `Pageable` for list endpoints
- **DTOs**: Separate request/response objects — never expose JPA entities
- **Error format**:
  ```json
  {
    "error": "VALIDATION_FAILED",
    "message": "Human-readable description",
    "details": [ { "field": "title", "reason": "must not be blank" } ]
  }
  ```
- **Documentation**: `springdoc-openapi` with Swagger UI at `/swagger-ui.html`

### Key resource endpoints

```
GET    /api/v1/modules                    # List course modules (public)
GET    /api/v1/modules/{id}/lessons       # Lessons in a module
GET    /api/v1/modules/{id}/lessons/{id}  # Lesson content + audio URL
GET    /api/v1/exercises/{id}             # Exercise definition
POST   /api/v1/exercises/{id}/submit      # Submit exercise answer
GET    /api/v1/progress                   # Current user's progress
POST   /api/v1/admin/modules              # Create module (admin)
PUT    /api/v1/admin/lessons/{id}         # Update lesson content (admin)
POST   /api/v1/admin/lessons/{id}/generate-audio  # Trigger TTS (admin)
POST   /api/v1/storage/upload             # Upload file (admin)
```

## Database Schema

```mermaid
erDiagram
    USERS {
        uuid id PK
        text google_id UK
        text email UK
        text display_name
        text avatar_url
        boolean email_verified
        timestamptz created_at
        timestamptz updated_at
        timestamptz deleted_at
    }

    MODULES {
        uuid id PK
        text title
        text description
        text thumbnail_url
        content_status status "draft|published|archived"
        int sort_order
        bool if_free_preview
        timestamptz created_at
        timestamptz updated_at
    }

    LESSONS {
        uuid id PK
        uuid module_id FK
        text title
        text content
        content_status status "draft|published|archived"
        content_format content_format "html|markdown"
        text video_url
        bool if_free_preview
        timestamptz created_at
        timestamptz updated_at
    }

    MODULE_LESSONS {
        uuid module_id PK, FK
        uuid lesson_id PK, FK
        int sort_order
        timestamptz created_at
    }

    USER_LESSON_PROGRESS {
        uuid user_id PK, FK
        uuid lesson_id PK, FK
        decimal progress_pct
        lesson_progress_status status "not_started|in_progress|completed"
        timestamptz started_at
        timestamptz completed_at
        timestamptz last_viewed_at
        timestamptz created_at
        timestamptz updated_at
    }

    MODULES ||--o{ MODULE_LESSONS : contains
    LESSONS ||--o{ MODULE_LESSONS : included_in
    USERS ||--o{ USER_LESSON_PROGRESS : tracks
    LESSONS ||--o{ USER_LESSON_PROGRESS : has
```

### Key design decisions

- **Soft deletes** via `deleted_at` (users) and `content_status` (content) — no hard deletes
- **`is_free_preview`** on both modules and lessons for granular visitor access
- **JSONB** for exercise options and submitted answers (flexible without extra join tables)
- **`sort_order`** integer on modules, lessons, attachments, questions for manual ordering
- **`ON DELETE SET NULL`** for `created_by` references (keep content if admin is removed)
- **Auto-updated `updated_at`** via PostgreSQL trigger on users, modules, lessons, exercises

### Database files

| File | Purpose |
|------|---------|
| [`backend/src/main/resources/db/migration/V1__initial_schema.sql`](backend/src/main/resources/db/migration/V1__initial_schema.sql) | Flyway V1 migration: extensions, enums, 5 tables (users, modules, lessons, module_lessons, user_lesson_progress), indexes, constraints, triggers |

Flyway migrations live in `backend/src/main/resources/db/migration/`.

## File Storage

MinIO provides S3-compatible object storage in Docker. The backend uses the AWS S3 SDK.

| Bucket | Contents |
|--------|----------|
| `audio` | TTS-generated voiceover MP3 files |
| `assets` | SVG animations, images, uploaded media |

### Migration path to AWS

| Phase | Storage | Config |
|-------|---------|--------|
| Local / Dev | MinIO in Docker Compose | `s3.endpoint: http://minio:9000` |
| Production (VPS) | MinIO in Docker Compose | `s3.endpoint: http://minio:9000` |
| Production (AWS) | AWS S3 | `s3.endpoint: https://s3.amazonaws.com` |

## Voiceover Pipeline

1. Admin writes/edits lesson text in the content editor
2. Admin clicks "Generate Audio" → backend receives request
3. Spring `@Async` method sends text to Google Cloud TTS API
4. Resulting MP3 is stored in MinIO (`audio` bucket)
5. `audio_url` on the lesson record is updated (column to be added in a future migration)
6. Frontend plays audio via standard `<audio>` element synced with content

## SVG Animations

- Simple movement animations for MVP (drone flyovers, part highlights)
- SVGs stored in the `assets` bucket or bundled with the frontend
- Animated via Angular animations or CSS keyframes
- Triggered by scroll position or lesson progress

## Content Management (post-MVP)

For MVP, admins manage content via admin API endpoints. A WYSIWYG editor (TipTap or similar) is planned for post-MVP to provide a richer editing experience. Content is stored as Markdown in the `lessons.content` column and rendered to HTML on the frontend.

## Docker Compose Services

```yaml
services:
  frontend       # Angular built + served via Nginx
  backend        # Spring Boot JAR
  postgres       # PostgreSQL 16
  minio          # MinIO object storage
```

## Gamification (MVP)

- **Progress tracking**: per-lesson and per-module completion percentage
- **Score recording**: exercise results stored in `user_exercise_attempts`, lesson progress in `user_lesson_progress`
- **Dashboard**: visual progress bars per module

Post-MVP: badges, achievements, streaks, leaderboards.
