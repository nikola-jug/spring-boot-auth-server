# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Skeleton OAuth2 Authorization Server built on Spring Authorization Server. Intended to serve as the in-house AS that the BFF registers as a confidential client with. Includes Thymeleaf for a custom login UI.

## Commands

```bash
# Start the database first
docker compose up -d

# For local dev, source secrets before starting (see .env.example)
source .env && ./mvnw spring-boot:run    # Start the server (Flyway runs automatically on startup)
./mvnw test               # Run tests
./mvnw clean package      # Build JAR
```

## Key Notes

- **Authorization server DSL** — `SecurityConfiguration` uses `http.oauth2AuthorizationServer(...)` (Spring Security 7.x DSL). Do **not** manually instantiate `new OAuth2AuthorizationServerConfigurer()` and use `.with()` — that's the old 5.x/6.x pattern and breaks `OAuth2AuthorizationCodeRequestAuthenticationProvider` setup in 7.x.
- **Login UI** — Custom Thymeleaf login page at `src/main/resources/templates/login.html`, served by `controller/LoginController`. The default security chain uses `formLogin(Customizer.withDefaults())` which picks up `/login`.
- **Flyway migrations** in `src/main/resources/db/migration/`:
  - `V1` — Spring Session JDBC schema (`SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`)
  - `V2` — Spring Authorization Server schema (`oauth2_registered_client`, `oauth2_authorization`, `oauth2_authorization_consent`)
  - `V3` — User schema (`users`, `authorities`) for the auth server's own user store
- **Client registration is DB-backed** via `AuthorizationServerConfiguration`, which reads from `OAuthClientRegistrationProperties` (`oauth.client.registration.*` in `application.yaml`) and upserts on startup via `JdbcRegisteredClientRepository`. Uses true upsert — if the client already exists, it is updated; if not, it is inserted. Changes to YAML (redirect URIs, scopes, token TTLs, etc.) are picked up on every restart without a DB wipe.
- **Spring Session JDBC** (`spring-boot-starter-session-jdbc`) enables horizontal scaling; `spring.session.jdbc.initialize-schema: never` — Flyway owns the schema.
- **Local PostgreSQL** on port `5433` via `docker-compose-database.yaml` in this directory. In Docker, connects via `spring-boot-auth-server-db:5432`.
- Runs on port **9000**. Issuer URI overridable via `AUTH_SERVER_ISSUER_URI` env var (consistent naming with BFF and Resource Server); in Docker set to `https://spring-boot-auth-server:9000`.
- Registered BFF clients: `spring-boot-web-bff` and `spring-boot-mobile-bff`, both with PKCE (S256) required, `requireAuthorizationConsent: false`, scopes `openid profile email`.
- Datasource defaults: `jdbc:postgresql://localhost:5433/spring-boot-auth-server` (overridable via `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`).
- **Client secrets** are injected via env vars (`WEB_BFF_CLIENT_SECRET`, `MOBILE_BFF_CLIENT_SECRET`) — no defaults, so startup fails if not set. Copy `.env.example` to `.env` for local dev. Docker Compose picks up `.env` automatically via `env_file`. In production, inject via your secret manager with a proper password encoder (BCrypt) instead of `{noop}`.
- This is a **standalone** IdP — it does not federate to Keycloak. The BFF registers with both separately.
