# Habit Tracker

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791)
![Build](https://img.shields.io/badge/build-Maven-C71A36)
[![CI](https://github.com/saulcone/habit-tracker/actions/workflows/ci.yml/badge.svg)](https://github.com/saulcone/habit-tracker/actions/workflows/ci.yml)

A habit tracking backend built with **Java 21 and Spring Boot**. Users define habits and log their progress day by day, with each day computed in the **user's own time zone**.

This is a learning project: the goal is to build a solid foundation in Spring Boot (persistence, migrations, services, REST API, testing, layered design) by implementing each layer deliberately and testing it.

> **Status:** the persistence layer, the service layer, the REST API (5 endpoints) with OpenAPI documentation, a basic web front end and a CI pipeline are complete and tested (see the [roadmap](#roadmap)).

## Tech stack

| Area | Technology |
|---|---|
| Language | Java 21 (virtual threads enabled) |
| Framework | Spring Boot 4.1 (Spring MVC, Spring Data JPA, Bean Validation) |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Testing | JUnit 5, Mockito, `@DataJpaTest`, `@WebMvcTest`, MockMvc |
| API docs | OpenAPI 3 with Swagger UI (springdoc-openapi) |
| Front end | Single static HTML page with vanilla JavaScript (no build step) |
| Build | Maven (wrapper included) |
| CI | GitHub Actions |
| Local infra | Docker Compose |

## Data model

```mermaid
erDiagram
    USERS ||--o{ HABITS : owns
    HABITS ||--o{ HABIT_LOGS : "is logged in"

    USERS {
        bigint id PK
        varchar email UK
        varchar timezone
        timestamptz created_at
    }
    HABITS {
        bigint id PK
        bigint user_id FK
        varchar name
        text description
        boolean is_active
        timestamptz created_at
    }
    HABIT_LOGS {
        bigint id PK
        bigint habit_id FK
        timestamptz completed_at
        date local_date
        varchar status
    }
```

Key design decisions:

- **Time-zone aware days.** A log stores both the exact moment (`completed_at`, a `timestamptz`) and the user's calendar day (`local_date`). The same instant can be "today" for one user and "tomorrow" for another, so the day is derived from the user's `timezone`.
- **One log per habit per day**, enforced by the database with a unique constraint on `(habit_id, local_date)`, not only in application code. This protects against race conditions between concurrent requests.
- **One habit name per user**, also enforced by a unique constraint on `(user_id, name)`.
- **Schema owned by Flyway, only validated by Hibernate** (`ddl-auto: validate`). If an entity and the schema drift apart, the application refuses to start.
- **Cascade deletes at database level**: deleting a user removes their habits, and deleting a habit removes its logs.
- **Lazy relationships** (`@ManyToOne(fetch = LAZY)`) to avoid loading related entities that are never used.

## API

Base path: `/api/v1`. Requests and responses are JSON.

| Method | Path | Description | Success | Errors |
|---|---|---|---|---|
| `POST` | `/users` | Register a user (email + IANA time zone) | `201` | `400`, `409` |
| `POST` | `/users/{userId}/habits` | Create a habit for a user | `201` | `400`, `404`, `409` |
| `GET` | `/users/{userId}/habits` | List the user's active habits, ordered by name | `200` | `400`, `404` |
| `POST` | `/habits/{habitId}/logs` | Log the habit for "today" in the user's time zone | `201` new, `200` already logged | `400`, `404`, `409` |
| `GET` | `/habits/{habitId}/logs?begin=YYYY-MM-DD&end=YYYY-MM-DD` | List a habit's logs between two dates (inclusive), ordered by date | `200` | `400`, `404` |

Interactive documentation is generated from the code with springdoc-openapi: open **Swagger UI** at `http://localhost:8080/swagger-ui.html` to explore and try the endpoints from the browser. The raw OpenAPI specification is available at `http://localhost:8080/v3/api-docs`.

**Logging is idempotent per day.** Calling `POST /habits/{habitId}/logs` twice on the same local day returns the existing log with `200` instead of an error. The first call returns `201`.

### Example

```bash
# Register a user
curl -i -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","timezone":"Europe/Madrid"}'

# Create a habit
curl -i -X POST http://localhost:8080/api/v1/users/1/habits \
  -H "Content-Type: application/json" \
  -d '{"name":"Read","description":"30 minutes a day"}'

# Log it for today (201 the first time, 200 afterwards)
curl -i -X POST http://localhost:8080/api/v1/habits/1/logs

# Logs for a date range
curl -i "http://localhost:8080/api/v1/habits/1/logs?begin=2026-11-01&end=2026-11-30"
```

### Error handling

All errors use the standard **Problem Details** format (RFC 9457, `application/problem+json`) with a human-readable `detail`:

```json
{
  "detail": "Habit's name already existing.",
  "instance": "/api/v1/users/1/habits",
  "status": 409,
  "title": "Conflict"
}
```

| Status | When |
|---|---|
| `400` | Invalid input: failed validation, malformed JSON, invalid time zone, non-numeric ids, missing or badly formatted parameters, `begin` after `end` |
| `404` | The user or the habit does not exist |
| `409` | Conflict with the current state: email already registered, habit name already used by that user, inactive habit, a log already created concurrently for that day |
| `500` | Anything unexpected. The response carries a **generic message only**: internal details (SQL, stack traces, constraint names) are logged on the server and never sent to the client |

Database unique constraints are translated into business errors (`409`) by the service layer; any other integrity violation is rethrown and ends up as a `500`.

## Web front end

A basic single-page UI is served by the application itself at `http://localhost:8080/`, so it shares the origin with the API and needs no CORS configuration. It lets you:

- create a user (the time zone is prefilled from the browser) or continue with an existing user id,
- create habits and list the active ones,
- mark a habit as done for today (`Done today` button) and see the last 30 days at a glance,
- browse the logs of a habit between two dates.

The session (user id and time zone) is kept in the browser's `localStorage`. There is no authentication, and the API has no endpoint to fetch a user by id, so when continuing with an existing id the page asks for the time zone.

## Project structure

```
src/main/java/habits/app
├── api
│   ├── controller   # REST controllers and the global exception handler
│   └── dto          # Request/response records (validation on input DTOs)
├── service          # Business rules, transactions
├── exception        # Business exceptions
├── domain           # JPA entities: User, Habit, HabitLog
└── repository       # Spring Data JPA repositories and derived queries

src/main/resources
├── application.yml
├── static           # Web front end (index.html)
└── db/migration     # Flyway migrations

src/test/java/habits/app
├── repository       # @DataJpaTest integration tests against PostgreSQL
├── service          # Unit tests with Mockito
└── api/controller   # @WebMvcTest tests with MockMvc
```

Layering rules: controllers are thin and only deal with HTTP and DTO mapping; services know nothing about HTTP (they return domain objects or throw business exceptions); the global `@RestControllerAdvice` maps exceptions to status codes in a single place.

## Getting started

### Prerequisites

- Java 21
- Docker and Docker Compose

### Run it

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the application (Flyway applies the migrations on startup)
./mvnw spring-boot:run
```

The web UI is then available at `http://localhost:8080/`, the API at `http://localhost:8080/api/v1` and Swagger UI at `http://localhost:8080/swagger-ui.html`. The database settings live in `src/main/resources/application.yml` and match the `docker-compose.yml` file. The credentials in this repository are for **local development only**.

### Run the tests

```bash
docker compose up -d   # the repository tests use the same local PostgreSQL
./mvnw test
```

## Continuous integration

A GitHub Actions workflow (`.github/workflows/ci.yml`) runs on every push to `main` and on every pull request targeting `main`. It starts a PostgreSQL 16 service container, sets up Java 21 and runs `./mvnw verify`, so the whole test suite (including the database tests) runs against a real PostgreSQL on each change.

## Testing approach

The project has three kinds of tests, one per layer:

- **Repository tests** (`@DataJpaTest`) run against **a real PostgreSQL** instead of an in-memory database, so they exercise the same types, constraints and behavior as the application. Each test runs in a transaction that is rolled back afterwards. They cover persistence round trips (flushing and clearing the persistence context before reading back), derived queries (including boundary dates and ordering), existence checks and database constraints.
- **Service tests** (Mockito) check the business rules in isolation: validation order, idempotent logging, translation of constraint violations into business exceptions, and that no repository is touched when a cheap local check already fails. Time is injected through a `Clock`, so tests use a fixed instant and check the same moment in different time zones (for example, 23:30 UTC is already the next day in Madrid but still the same day in Los Angeles).
- **Controller tests** (`@WebMvcTest` + MockMvc) check the HTTP contract with the service mocked: status codes, JSON bodies, validation errors, malformed input, and that internal error messages never reach the client.

## Roadmap

- [x] Database schema and Flyway migrations
- [x] JPA entities
- [x] Repositories with derived queries
- [x] Repository integration tests against PostgreSQL
- [x] Service layer: business rules, transactions and unit tests with Mockito
    - [x] User registration (unique email)
    - [x] Habit creation
    - [x] Log a habit for "today" in the user's time zone
- [x] REST API with DTOs and request validation
- [x] Global error handling (`@RestControllerAdvice`)
- [x] Read endpoints (active habits, logs between two dates)
- [x] CI pipeline (GitHub Actions)
- [x] Basic web front end
- [x] API documentation (OpenAPI / Swagger UI)

Out of scope for now: authentication and CORS.

## Author

Built by [@saulcone](https://github.com/saulcone).