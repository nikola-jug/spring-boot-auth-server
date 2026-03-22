# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Skeleton OAuth2 Authorization Server built on Spring Authorization Server. Intended to serve as the in-house AS that the BFF registers as a confidential client with. Includes Thymeleaf for a custom login UI.

## Commands

```bash
./mvnw spring-boot:run    # Start the server
./mvnw test               # Run tests
./mvnw clean package      # Build JAR
```

## Key Notes

- **No registered clients yet** — OAuth2 client registration (in-memory or DB-backed) still needs to be implemented.
- **No login UI yet** — Thymeleaf + `thymeleaf-extras-springsecurity6` are on the classpath but no templates exist under `src/main/resources/templates/`.
- **No Flyway migrations yet** — `src/main/resources/db/migration/` exists but is empty; PostgreSQL schema for users and OAuth2 client data is not yet defined.
- **Minimal application.yaml** — all runtime configuration (DB URL, OAuth2 endpoints) must come from environment variables or an external config source.
- `@ConfigurationProperties` support is on the classpath (configuration processor) for future externalized config binding.
