# Roomi.kz — backend

Roommate-matching web app for students in Almaty. This repository contains the Spring Boot backend.

**Stack:** Java 25, Spring Boot 4, Spring Web MVC, Spring Data JPA, Spring Security (JWT, HS256), PostgreSQL, Maven.

## Requirements

- JDK 25
- Docker (for PostgreSQL)

## Run locally

```bash
docker compose up -d
./mvnw spring-boot:run
```

On Windows use `mvnw.cmd spring-boot:run`.

The API starts on `http://localhost:8080`. PostgreSQL runs on `localhost:5432` (db / user / password: `roomi`).

By default `MAIL_ENABLED=false`: emails are not sent, the confirmation link is printed to the application log:

```
Mail disabled. Confirmation link for student@example.com: http://localhost:5173/confirm?token=...
```

## Tests

```bash
./mvnw test
```

Tests use an in-memory H2 database (PostgreSQL mode) and do not need Docker.

## Environment variables

| Variable        | Default                                        | Description                                         |
|-----------------|------------------------------------------------|-----------------------------------------------------|
| `DB_URL`        | `jdbc:postgresql://localhost:5432/roomi`       | JDBC URL                                            |
| `DB_USERNAME`   | `roomi`                                        | DB user                                             |
| `DB_PASSWORD`   | `roomi`                                        | DB password                                         |
| `JWT_SECRET`    | dev value                                      | HS256 secret, **at least 32 bytes**. Change in prod |
| `JWT_TTL`       | `1h`                                           | Access token lifetime (`15m`, `1h`, `1d`, ...)      |
| `MAIL_ENABLED`  | `false`                                        | `true` to really send emails via Gmail SMTP         |
| `MAIL_USERNAME` | empty                                          | Gmail address used for SMTP login                   |
| `MAIL_PASSWORD` | empty                                          | Gmail App Password (16 characters)                  |
| `MAIL_FROM`     | `Roomi.kz <no-reply@roomi.kz>`                 | "From" header. For Gmail use your Gmail address     |
| `CONFIRM_URL`   | `http://localhost:5173/confirm`                | Frontend page; `?token=...` is appended             |
| `CORS_ORIGINS`  | `http://localhost:5173,http://localhost:3000`  | Comma-separated allowed origins                     |

Example:

```bash
MAIL_ENABLED=true MAIL_USERNAME=roomi.kz@gmail.com MAIL_PASSWORD=abcdefghijklmnop MAIL_FROM=roomi.kz@gmail.com ./mvnw spring-boot:run
```

## Gmail App Password

Gmail does not accept your normal password for SMTP. You need an App Password:

1. Open https://myaccount.google.com/security and enable **2-Step Verification**.
2. Open https://myaccount.google.com/apppasswords.
3. Enter a name (for example `Roomi`) and click **Create**.
4. Copy the 16-character password (without spaces) into `MAIL_PASSWORD`.
5. Set `MAIL_USERNAME` and `MAIL_FROM` to the same Gmail address and `MAIL_ENABLED=true`.

## API

All errors have the same format:

```json
{ "message": "Validation failed", "errors": { "email": "Email is invalid" } }
```

| Method | Path                          | Auth   | Body                  | Success                                         | Errors                                                          |
|--------|-------------------------------|--------|-----------------------|-------------------------------------------------|-----------------------------------------------------------------|
| POST   | `/api/auth/register`          | —      | `{email, password}`   | `201 {message}`                                 | `400` validation, `409` email already registered                |
| GET    | `/api/auth/confirm?token=...` | —      | —                     | `200 {message}`                                 | `400` link invalid, `400` link expired                          |
| POST   | `/api/auth/resend`            | —      | `{email}`             | `200 {message}` (always, does not reveal email) | `400` validation                                                |
| POST   | `/api/auth/login`             | —      | `{email, password}`   | `200 {accessToken, tokenType, expiresIn}`       | `400` validation, `401` wrong email or password, `403` not confirmed |
| GET    | `/api/users/me`               | Bearer | —                     | `200 {id, email, createdAt}`                    | `401` no / invalid token                                        |

Rules:

- Email is trimmed and stored in lowercase, uniqueness is case-insensitive.
- Password: 8–72 characters, stored as a BCrypt hash.
- Confirmation token: random 32 bytes (base64url), valid for 24 hours. A new `resend` invalidates previous links.
- JWT: `sub` = user id, `email` claim, issuer `roomi`. Send it as `Authorization: Bearer <token>`.

Example requests are in [`api.http`](api.http) (IntelliJ HTTP Client / VS Code REST Client).
