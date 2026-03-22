# spring-boot-auth-server

In-house OAuth2 Authorization Server built on Spring Authorization Server. Acts as one of two identity providers the BFF registers with. Provides a custom Thymeleaf login UI and issues JWTs to the BFF on behalf of authenticated users.

## Purpose

- Issues authorization codes and tokens to registered BFF clients
- Authenticates users against its own PostgreSQL-backed user store
- Exposes OIDC discovery at `/.well-known/openid-configuration`
- Operates standalone — it does not federate to Keycloak

## Architecture

```
Browser
   │  redirected here by BFF for login
   ▼
spring-boot-auth-server (port 9000, HTTPS)
   │  custom Thymeleaf login page (/login)
   │  issues authorization code
   ▼
spring-boot-web-bff / spring-boot-mobile-bff
   │  exchanges code for tokens (PKCE, client_secret_basic)
   ▼
spring-boot-resource-server  (validates JWTs via OIDC discovery)
```

All state is persisted in PostgreSQL:

| Table group | Purpose |
|---|---|
| `SPRING_SESSION` / `SPRING_SESSION_ATTRIBUTES` | User login sessions (Flyway V1) |
| `oauth2_registered_client` / `oauth2_authorization` / `oauth2_authorization_consent` | Authorization Server state (Flyway V2) |
| `users` / `authorities` | User credentials and roles (Flyway V3) |

## Running

```bash
# Start the database
docker compose -f docker-compose-database.yaml up -d

# Create .env from the example (first time only)
cp .env.example .env

# Build and start the server
./mvnw clean package -DskipTests
docker compose up -d --build
```

For local dev without Docker:

```bash
source .env && ./mvnw spring-boot:run
```

The server is available at `https://spring-boot-auth-server:9000`.

## Configuration

| Property | Env var | Default |
|---|---|---|
| Issuer URI | `AUTH_SERVER_ISSUER_URI` | `https://spring-boot-auth-server:9000` |
| Database URL | `DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/spring-boot-auth-server` |
| Web BFF client secret | `WEB_BFF_CLIENT_SECRET` | *(required — no default)* |
| Mobile BFF client secret | `MOBILE_BFF_CLIENT_SECRET` | *(required — no default)* |

Client secrets are provided via `.env` (gitignored). Copy `.env.example` to `.env` to get started. The `{noop}` prefix in the example values means plaintext comparison — acceptable for local dev only. In production, use a proper password encoder (BCrypt) and inject secrets via your platform's secret manager.

Registered clients are configured under `oauth.client.registrations.*` in `application.yaml`:

- `spring-boot-web-bff` — web BFF client (redirect to port 8080)
- `spring-boot-mobile-bff` — mobile BFF client (redirect to port 8082)

## Caveats

- **Client registration upserts on startup** — `AuthorizationServerConfiguration` performs a true upsert: if a client already exists it is updated, otherwise it is inserted. Changes to redirect URIs, scopes, token TTLs, etc., are applied automatically on the next restart — no DB wipe required.

- **Authorization Server DSL** — `SecurityConfiguration` uses `http.oauth2AuthorizationServer(...)` (Spring Security 7.x DSL). Do **not** manually instantiate `new OAuth2AuthorizationServerConfigurer()` and apply it via `.with()` — that is the old 5.x/6.x pattern and breaks `OAuth2AuthorizationCodeRequestAuthenticationProvider` setup in 7.x.

- **Flyway owns the schema** — `spring.session.jdbc.initialize-schema: never` is intentional. Do not change it.

- **HTTPS is required** — the JWTs issued here carry the HTTPS issuer URI. The BFF and Resource Server perform OIDC discovery against that URI, so it must match exactly what the server advertises.
