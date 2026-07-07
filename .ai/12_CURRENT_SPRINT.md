# Sprint 03 - Session & Device Management

Status: COMPLETED on 2026-07-07

## Objective

Implement user session and device management to track login sessions across devices, display active sessions in a dashboard, and allow users to revoke individual or all sessions.

## Requirements

### Session Creation on Login/Refresh
- A session record is created automatically for every successful login and token refresh.
- Each session captures device context: browser, operating system, device name (parsed from User-Agent), and IP address.
- Sessions are linked to their associated refresh tokens for simultaneous revocation.

### Session Listing
- `GET /api/v1/sessions` returns all active (non-revoked, non-expired) sessions for the authenticated user.
- The current session (used to make the request) is marked with `isCurrent: true`.
- Response includes device info, timestamps, and revocation status.

### Current Session
- `GET /api/v1/sessions/current` returns details about the session used to make the request.
- Uses the `sessionId` claim embedded in the JWT access token.

### Session Revocation
- `DELETE /api/v1/sessions/{sessionId}` revokes a specific session by ID (with user ownership check).
- `DELETE /api/v1/sessions/current` revokes the current session.
- `DELETE /api/v1/sessions/all` revokes all active sessions for the user.
- Revoking a session also revokes its associated refresh token.
- `404 Not Found` for non-existent or non-owned sessions.

## Architecture Decisions

### Session ID in JWT
- The JWT access token carries a `sessionId` claim, enabling the API to identify the current session without additional queries.
- `JwtService.createAccessToken` accepts an optional `sessionId` parameter.

### User-Agent Parsing
- A simple string-matching parser (`UserAgentParser`) extracts browser, OS, and device name.
- Uses `@Component` for dependency injection.
- No external UA parsing libraries.

### JWT Authentication Filter
- `JwtAuthenticationFilter` extracts and validates Bearer tokens from the `Authorization` header.
- Sets `UsernamePasswordAuthenticationToken` with `userId` as principal and `sessionId` as credentials.
- Registered in `SecurityConfig` before `UsernamePasswordAuthenticationFilter`.

## Files Changed

### New Files
- `backend/server/src/main/resources/db/migration/V4__Create_sessions_table.sql`
- `sessions/entity/Session.java`
- `sessions/repository/SessionRepository.java`
- `sessions/dto/SessionResponse.java`
- `sessions/exception/SessionNotFoundException.java`
- `sessions/service/SessionService.java`
- `sessions/service/UserAgentParser.java`
- `sessions/controller/SessionController.java`
- `config/JwtAuthenticationFilter.java`
- `sessions/controller/SessionControllerIntegrationTests.java`
- `sessions/service/SessionServiceTests.java`
- `sessions/service/UserAgentParserTests.java`

### Modified Files
- `auth/service/AuthenticationService.java`: accepts `ipAddress` and `userAgent`, creates sessions on login/refresh
- `auth/controller/AuthenticationController.java`: forwards HTTP request info to service
- `config/SecurityConfig.java`: registers JWT filter and session endpoint permissions
- `common/exception/GlobalExceptionHandler.java`: handles `SessionNotFoundException`
- `auth/jwt/JwtService.java`: added `createAccessToken(UUID, String, UUID)` overload

## Test Coverage

- `UserAgentParserTests`: 5 test cases (Chrome/Windows, Firefox/macOS, Safari/iOS, Edge/Android, null, empty)
- `SessionServiceTests`: 5 tests (create, list with current flag, revoke, wrong user, revoke all)
- `SessionControllerIntegrationTests`: 9 tests (login creates session, list, get current, revoke by id, revoke nonexistent, revoke current, revoke all, multiple logins, unauthenticated)
- `AuthenticationServiceTests`: updated stubs for new 3-param `createAccessToken` and `createSession` return

## Verification

- `./gradlew.bat build` succeeds with 51 tests passing.
- Sessions are created on login and refresh with device context.
- Session ID is embedded in JWT access tokens.
- Session revocation cascades to refresh token revocation.
- Current session is correctly identified via JWT `sessionId` claim.
