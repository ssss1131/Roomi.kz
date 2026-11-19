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
Mail disabled. Confirmation link for student@example.com: http://localhost:3000/confirm?token=...
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
| `CONFIRM_URL`   | `http://localhost:3000/confirm`                | Frontend page; `?token=...` is appended             |
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
| GET    | `/api/users/me`               | Bearer | —                     | `200 User` (with `profile`)                     | `401` no / invalid token                                        |
| PUT    | `/api/users/me`               | Bearer | `Profile`             | `200 User`                                      | `400` validation, `401`                                         |
| GET    | `/api/rooms`                  | —      | —                     | `200 {items: RoomSummary[]}` (not taken only)   | —                                                               |
| GET    | `/api/rooms/{id}`             | Bearer | —                     | `200 RoomDetails`                               | `401`, `404` not found or taken (owner still sees it)           |
| POST   | `/api/rooms`                  | Bearer | `RoomInput`           | `201 RoomDetails`                               | `400` validation, `401`, `409` profile incomplete               |
| PUT    | `/api/rooms/{id}`             | Owner  | `RoomInput`           | `200 RoomDetails`                               | `400` validation, `401`, `403` not the owner, `404`             |
| DELETE | `/api/rooms/{id}`             | Owner  | —                     | `204`                                           | `401`, `403` not the owner, `404`                               |
| GET    | `/api/users/me/rooms`         | Bearer | —                     | `200 {items: MyRoomSummary[]}` (incl. taken)    | `401`                                                           |
| GET    | `/api/roommates`              | Bearer | —                     | `200 {items: RoommateSummary[]}`                | `401`                                                           |
| GET    | `/api/roommates/{userId}`     | Bearer | —                     | `200 PublicProfile`                             | `401`, `404` not found or not looking for a roommate            |

The models (`User`, `Profile`, `RoomInput`, `RoomDetails`, ...) follow the frontend API contract ("Roomi API Contract").

Rules:

- Email is trimmed and stored in lowercase, uniqueness is case-insensitive.
- Password: 8–72 characters, stored as a BCrypt hash.
- Confirmation token: random 32 bytes (base64url), valid for 24 hours. A new `resend` invalidates previous links.
- JWT: `sub` = user id, `email` claim, issuer `roomi`. Send it as `Authorization: Bearer <token>`.
- Enums are lowercase: gender `male` / `female`, rules `no_smoking`, `no_pets`, `keep_quiet`, `keep_tidy`.
- Optional fields are always present in responses (`null` when empty). Request strings are trimmed, blank optional strings become `null`, unknown and read-only fields are ignored.
- Profile: name, gender and at least one contact are required on every save. `minPrice` / `maxPrice` are both set or both empty. Field errors use dotted keys for nested fields, e.g. `contacts.telegram`, and `contacts` when no contact is given.
- Rooms: `POST /api/rooms` returns `409` until the user has saved a profile. `viewsCount` grows by 1 on each `GET /api/rooms/{id}` by a logged-in user who is not the owner.
- Ordering: rooms newest first, roommates highest user id first. `email` and `createdAt` are only returned by `GET /api/users/me`.

Example requests are in [`api.http`](api.http) (IntelliJ HTTP Client / VS Code REST Client).
