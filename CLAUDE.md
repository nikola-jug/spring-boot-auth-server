# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Skeleton OAuth2 Authorization Server built on Spring Authorization Server. Intended to serve as the in-house AS that the BFF registers as a confidential client with. Includes Thymeleaf for a custom login UI.

## Commands

```bash
# Start the database first
docker compose up -d

./mvnw spring-boot:run    # Start the server (Flyway runs automatically on startup)
./mvnw test               # Run tests
./mvnw clean package      # Build JAR
```

## Key Notes

- **Authorization server DSL** — `SecurityConfiguration` uses `http.oauth2AuthorizationServer(...)` (Spring Security 7.x DSL). Do **not** manually instantiate `new OAuth2AuthorizationServerConfigurer()` and use `.with()` — that's the old 5.x/6.x pattern and breaks `OAuth2AuthorizationCodeRequestAuthenticationProvider` setup in 7.x.
- **Login UI** — Custom Thymeleaf login page at `src/main/resources/templates/login.html`, served by `LoginController`. The default security chain uses `formLogin(Customizer.withDefaults())` which picks up `/login`.
- **Flyway migrations** in `src/main/resources/db/migration/`:
  - `V1` — Spring Session JDBC schema (`SPRING_SESSION`, `SPRING_SESSION_ATTRIBUTES`)
  - `V2` — Spring Authorization Server schema (`oauth2_registered_client`, `oauth2_authorization`, `oauth2_authorization_consent`)
  - `V3` — User schema (`users`, `authorities`) for the auth server's own user store
- **Client registration is DB-backed** via `AuthorizationServerConfiguration`, which reads from `OAuthClientRegistrationProperties` (`oauth.client.registration.*` in `application.yaml`) and upserts on startup via `JdbcRegisteredClientRepository`. Registration is **if-not-exists** — changes to YAML are not applied if the client already exists in DB.
- **Spring Session JDBC** (`spring-boot-starter-session-jdbc`) enables horizontal scaling; `spring.session.jdbc.initialize-schema: never` — Flyway owns the schema.
- **Local PostgreSQL** on port `5433` via `docker-compose-database.yaml` in this directory. In Docker, connects via `spring-boot-auth-server-db:5432`.
- Runs on port **9000**. Issuer URI overridable via `ISSUER_URI` env var; in Docker set to `http://spring-boot-auth-server:9000`.
- Registered BFF clients: `spring-boot-web-bff` and `spring-boot-mobile-bff`, both with PKCE (S256) required, `requireAuthorizationConsent: false`, scopes `openid profile email`.
- Datasource defaults: `jdbc:postgresql://localhost:5433/spring-boot-auth-server` (overridable via `DATASOURCE_URL`, `DATASOURCE_USERNAME`, `DATASOURCE_PASSWORD`).
- This is a **standalone** IdP — it does not federate to Keycloak. The BFF registers with both separately.
