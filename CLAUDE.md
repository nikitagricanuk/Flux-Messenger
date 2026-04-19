# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## CRITICAL RULES
- NEVER delete or overwrite working tests without explicit confirmation
- NEVER delete files or folders without explicit confirmation
- ALWAYS run tests after any code change
- ALWAYS do git checkpoint before big changes
- One task at a time. DON'T work on multiple things at once
- Not sure – ask. Don't guess

## Commands

### Backend
```bash
./gradlew :apps:backend:bootRun        # Run locally
./gradlew :apps:backend:bootJar        # Build JAR
./gradlew :apps:backend:compileJava    # Compile only
```

### Android
```bash
./gradlew :apps:android:app:build          # Build APK
./gradlew :apps:android:app:installDebug   # Install on emulator/device
```

### Infrastructure
```bash
docker compose -f infra/docker/compose.yaml up   # Start Postgres + backend
docker compose -f infra/docker/compose.yaml down  # Stop
```

### Full project
```bash
./gradlew build   # Build all modules
```

## Architecture

Multi-module Gradle project (Kotlin DSL). Modules:

- **`apps/backend`** — Spring Boot 4 REST API (Java 21, PostgreSQL)
- **`apps/android`** — Android app (minSdk 26, Java 11)
- **`shared/api-contract`** — Shared DTOs and domain models (Java 17)
- **`shared/core`** — Shared utilities (Java 17)
- **`shared/client-sdk`** — Wraps api-contract + core into a single SDK dependency

The Android app and backend both depend on `shared/api-contract` for shared model types.

### Backend (`apps/backend`)

Package root: `ru.flux.flux`

**Security flow**: every request goes through `JwtAuthenticationFilter` (extracts Bearer token, validates via `JwtService`, populates `SecurityContext`). `/api/auth/**` and Swagger endpoints are public; everything else requires authentication.

**Auth**: `POST /api/auth/sign-up` and `POST /api/auth/sign-in` → `AuthController` → `AuthenticationService`. Sign-in uses Spring's `AuthenticationManager` (phone as username). Returns `JwtAuthenticationResponse` with both access token (15 min) and refresh token (7 days).

**User identity**: `User` implements `UserDetails`. `getUsername()` returns `phone` (the Spring Security principal). The display handle field is `username`; access it via `getHandle()` to avoid confusion with `UserDetails.getUsername()`.

**JWT config** in `application.yaml`:
```yaml
jwt:
  secret: ...
  expiration-ms: 900000
  refresh-expiration-ms: 604800000
```

**Key beans in `SecurityConfig`**: `PasswordEncoder` (BCrypt), `AuthenticationProvider` (`DaoAuthenticationProvider` constructed with `UserDetailsService`), `AuthenticationManager`. `JwtAuthenticationFilter` is prevented from double-registration via `FilterRegistrationBean(enabled=false)`.

### Android (`apps/android`)

HTTP client: Retrofit 3 + Gson, configured in `RetrofitClient`. API interface: `ApiService`. Entry point: `MainActivity`. Auth UI: `LoginActivity`.

Cleartext HTTP traffic is enabled (`android:usesCleartextTraffic="true"`) for dev against the local backend.

## Agents
- Use `planner` agent for planning
- Use `tester` agent after changes in source code
- Use `code-reviewer` agent before committing