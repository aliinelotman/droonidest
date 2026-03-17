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

| Tool   | Version |
|--------|---------|
| Node.js | >= 22   |
| npm     | >= 11   |
| Java    | 21 LTS  |
| Maven   | >= 3.9  |

## Getting Started

### Frontend

```bash
cd frontend
npm install
npm start          # dev server on http://localhost:4200
```

### Backend

```bash
cd backend
./mvnw spring-boot:run   # dev server on http://localhost:8080
```

## Documentation

See [ARCHITECTURE.md](ARCHITECTURE.md), [PRODUCT.md](PRODUCT.md), and [CONVENTIONS.md](CONVENTIONS.md).
