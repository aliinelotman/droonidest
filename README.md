# Droonidest

Monorepo with an Angular frontend and a Java Spring Boot backend.

## Structure

```
├── frontend/          # Angular 19 (standalone components, SCSS)
├── backend/           # Spring Boot 3.4 / Java 21 (Maven)
├── ARCHITECTURE.md    # System design, service boundaries, data flow
├── PRODUCT.md         # Features, user stories, acceptance criteria
└── CONVENTIONS.md     # Coding standards, naming, commit format, PR rules
```

## Prerequisites

| Tool          | Version  |
|---------------|----------|
| Docker Desktop | latest  |
| Node.js       | >= 22    |
| npm           | >= 11    |
| Java          | 21 LTS   |
| Gradle        | >= 8     |

## Getting Started

### 1. Set up environment variables

Copy the example env file and fill in the required values:

```bash
cp .env.example .env
```

Required values to fill in:

| Variable | Where to get it |
|----------|-----------------|
| `GOOGLE_CLIENT_ID` | Google Cloud Console → Credentials → OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google Cloud Console → Credentials → OAuth client ID |
| `JWT_SECRET` | Generate with `openssl rand -base64 32` |
| `S3_ACCESS_KEY` | Same value as `MINIO_ROOT_USER` |
| `S3_SECRET_KEY` | Same value as `MINIO_ROOT_PASSWORD` |

Also register `http://localhost:4200/auth/callback` as an authorized redirect URI in your Google OAuth client.

### 2. Start with Docker

```bash
docker compose up --build --watch
```

| Service   | URL                                    |
|-----------|----------------------------------------|
| Frontend  | http://localhost:4200                  |
| Backend   | http://localhost:8080                  |
| Swagger   | http://localhost:8080/swagger-ui.html  |
| MinIO     | http://localhost:9001                  |

## Documentation

See [ARCHITECTURE.md](ARCHITECTURE.md), [PRODUCT.md](PRODUCT.md), and [CONVENTIONS.md](CONVENTIONS.md).
