# Sprint 04 - Role-Based Access Control (RBAC)

Status: COMPLETED on 2026-07-07

## Objective

Implement a production-ready RBAC system supporting users, roles, and permissions, designed so future features (Organizations, OAuth) can build on it.

## Requirements

### Database
- Create V5 Flyway migration with `roles`, `permissions`, `user_roles`, and `role_permissions` tables.
- Seed `ROLE_USER` and `ROLE_ADMIN` roles.
- Seed 6 permissions: `USER_READ`, `USER_WRITE`, `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`, `ROLE_WRITE`.
- `ROLE_USER` receives `SESSION_READ`, `SESSION_REVOKE`, `ROLE_READ`.
- `ROLE_ADMIN` receives all permissions.

### Domain Model
- Entities: `Role`, `Permission`, `UserRole`, `RolePermission`.
- Repositories with `JOIN FETCH` queries to avoid lazy loading issues.
- DTOs exposing only safe fields (id, name, description).

### Authorization
- Integrated with Spring Security via `@EnableMethodSecurity`.
- `JwtAuthenticationFilter` loads roles/permissions on every authenticated request.
- Roles stored as `ROLE_` authorities (`ROLE_USER`), permissions stored as-is (`SESSION_READ`).
- Supports both `hasRole()` and `hasAuthority()`.

### Default Behavior
- Every registered user automatically receives `ROLE_USER`.
- No administrator is automatically created.

### API
- `GET /api/v1/roles` — read-only, requires authentication.
- `GET /api/v1/permissions` — read-only, requires authentication.
- No role/permission modification endpoints.

## Architecture Decisions

### Authority Loading
- `UserAuthorityService` loads authorities from the database on every authenticated request using `JOIN FETCH` queries to prevent `LazyInitializationException`.
- Roles are prefixed with `ROLE_` for `hasRole()` compatibility.
- Permissions are stored as-is for `hasAuthority()` compatibility.

### Method-Level Security
- `@EnableMethodSecurity` is configured on `SecurityConfig`.
- Controllers use `@PreAuthorize("isAuthenticated()")` for declarative access control.

### Seed Data Strategy
- Seed data is inserted via Flyway migration `V5` for production/PostgreSQL.
- Integration tests seed data manually in `@BeforeEach` since Flyway is disabled in the H2 test profile.

### Registration Integration
- `RegistrationService` injects `RoleRepository` and `UserRoleRepository`.
- After saving the user, the service looks up `ROLE_USER` and creates a `UserRole` join record.

## Files Changed

### New Files
- `backend/server/src/main/resources/db/migration/V5__Create_rbac_tables.sql`
- `authorization/entity/Role.java`
- `authorization/entity/Permission.java`
- `authorization/entity/UserRole.java`
- `authorization/entity/RolePermission.java`
- `authorization/repository/RoleRepository.java`
- `authorization/repository/PermissionRepository.java`
- `authorization/repository/UserRoleRepository.java`
- `authorization/repository/RolePermissionRepository.java`
- `authorization/dto/RoleResponse.java`
- `authorization/dto/PermissionResponse.java`
- `authorization/service/RoleService.java`
- `authorization/service/PermissionService.java`
- `authorization/service/UserAuthorityService.java`
- `authorization/controller/RoleController.java`
- `authorization/controller/PermissionController.java`
- `authorization/service/RoleServiceTests.java`
- `authorization/service/PermissionServiceTests.java`
- `authorization/service/UserAuthorityServiceTests.java`
- `authorization/controller/AuthorizationControllerIntegrationTests.java`

### Modified Files
- `auth/service/RegistrationService.java`: injects role repositories, assigns ROLE_USER on registration
- `config/JwtAuthenticationFilter.java`: injects `UserAuthorityService`, sets authorities in security context
- `config/SecurityConfig.java`: adds `@EnableMethodSecurity`, adds role/permission endpoint rules
- `auth/service/RegistrationServiceTests.java`: adds mocks for RoleRepository and UserRoleRepository

## Test Coverage

- `RoleServiceTests`: 2 tests (returns all roles, returns empty list)
- `PermissionServiceTests`: 2 tests (returns all permissions, returns empty list)
- `UserAuthorityServiceTests`: 2 tests (loads role and permission authorities, returns empty for user with no roles)
- `AuthorizationControllerIntegrationTests`: 5 tests (user can list roles, user can list permissions, unauthenticated to roles returns forbidden, unauthenticated to permissions returns forbidden, roles response does not expose internal mappings)
- `RegistrationServiceTests`: updated to verify role assignment on registration

## Verification

- `./gradlew.bat build` succeeds with 62 tests passing.
- Flyway V5 migration runs successfully with seed data.
- Registered users automatically receive ROLE_USER.
- Authenticated users can list roles and permissions.
- Unauthenticated requests to RBAC endpoints return 403 Forbidden.
- Role and permission responses only expose id, name, description.
- JWT authentication filter correctly loads authorities on every request.
