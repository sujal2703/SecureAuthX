# Sprint 05 - Organizations & Multi-Tenancy Foundation

Status: COMPLETED on 2026-07-07

## Objective

Implement a multi-tenancy foundation with organizations, auto-created personal organizations on registration, and organization-level roles (OWNER, ADMIN, MEMBER) separate from global RBAC roles.

## Requirements

### Database
- Create V6 Flyway migration with `organizations` and `organization_members` tables.
- `organizations` columns: id, name, slug (unique), is_personal, created_at, updated_at.
- `organization_members` columns: id, organization_id (FK), user_id (FK), role (OWNER/ADMIN/MEMBER), created_at.
- Unique constraint on (organization_id, user_id).
- Unique index on slug.

### Domain Model
- Entities: `Organization`, `OrganizationMember`, `OrganizationRole` enum (OWNER, ADMIN, MEMBER).
- Repositories with `JOIN FETCH` queries to avoid lazy loading issues.
- DTOs exposing safe fields (id, name, slug, isPersonal, role, createdAt, updatedAt).

### Organization Service
- `createPersonalOrganization(User)`: creates a personal org with slug from user's email prefix.
- `createOrganization(User, CreateOrganizationRequest)`: creates a non-personal org with slug from name.
- `getOrganizationsForUser(User)`: returns all orgs the user belongs to.
- `getOrganizationForUser(UUID, User)`: returns a single org if the user is a member.
- `updateOrganization(UUID, UpdateOrganizationRequest, User)`: updates org metadata, enforces OWNER/ADMIN role check.
- Slug generation: lowercase, replace spaces with hyphens, strip non-alphanumeric, deduplicate on collision.

### API
- `GET /api/v1/organizations` — list user's organizations.
- `GET /api/v1/organizations/current` — get personal organization.
- `POST /api/v1/organizations` — create a new organization.
- `PATCH /api/v1/organizations/{organizationId}` — update organization (OWNER/ADMIN only).
- All endpoints require `isAuthenticated()`.

### Default Behavior
- Every registered user automatically receives a personal organization with role `OWNER`.
- Personal organizations have `isPersonal = true`.
- No invitation, member management, or deletion endpoints in this sprint.

## Architecture Decisions

### Roles Separation
- Organization roles (OWNER, ADMIN, MEMBER) are stored in `organization_members.role`.
- Global RBAC roles (`ROLE_USER`, `ROLE_ADMIN`) remain in the `roles` table.
- These are never merged or crossed.

### Personal Organization Creation
- `RegistrationService.register()` calls `OrganizationService.createPersonalOrganization()`.
- Personal org slug is derived from the email prefix (first part before @).
- If the slug already exists, a numeric suffix is appended for deduplication.

### Access Control
- Organization endpoints use `@PreAuthorize("isAuthenticated()")`.
- Per-org role checks are performed in the service layer, not at the HTTP security level.
- `OrganizationService.getOrganizationForUser()` verifies membership and throws `OrganizationNotFoundException` (404) or `OrganizationAccessDeniedException` (403).

### Exception Handling
- `OrganizationNotFoundException` → 404 via `GlobalExceptionHandler`.
- `OrganizationAccessDeniedException` → 403 via `GlobalExceptionHandler`.

### Slug Generation
- Lowercase the name.
- Replace spaces and consecutive hyphens with a single hyphen.
- Strip non-alphanumeric characters except hyphens.
- Trim leading/trailing hyphens.
- Append numeric suffix if slug already exists.

## Files Changed

### New Files
- `backend/server/src/main/resources/db/migration/V6__Create_organizations_tables.sql`
- `organization/entity/OrganizationRole.java`
- `organization/entity/Organization.java`
- `organization/entity/OrganizationMember.java`
- `organization/repository/OrganizationRepository.java`
- `organization/repository/OrganizationMemberRepository.java`
- `organization/dto/CreateOrganizationRequest.java`
- `organization/dto/UpdateOrganizationRequest.java`
- `organization/dto/OrganizationResponse.java`
- `organization/exception/OrganizationNotFoundException.java`
- `organization/exception/OrganizationAccessDeniedException.java`
- `organization/service/OrganizationService.java`
- `organization/controller/OrganizationController.java`
- `organization/service/OrganizationServiceTests.java`
- `organization/controller/OrganizationControllerIntegrationTests.java`

### Modified Files
- `auth/service/RegistrationService.java`: injects OrganizationService, creates personal org on registration
- `config/SecurityConfig.java`: adds `/api/v1/organizations/**` authenticated
- `common/exception/GlobalExceptionHandler.java`: handles OrganizationNotFoundException (404) and OrganizationAccessDeniedException (403)
- `auth/service/RegistrationServiceTests.java`: adds mock for OrganizationService
- `sessions/controller/SessionControllerIntegrationTests.java`: adds cleanup for organization and user_role tables in @BeforeEach (shared H2 context)
- `organization/controller/OrganizationControllerIntegrationTests.java`: adds UserRoleRepository cleanup in @BeforeEach

## Test Coverage

- `OrganizationServiceTests`: 6 tests (create personal org, create org, list orgs, get current org, update org as OWNER, update org as MEMBER throws)
- `OrganizationControllerIntegrationTests`: 8 tests (list orgs, get current, create org returns 201, update as owner, unauthenticated returns 403, member cannot update, registration creates personal org, slug generation)
- Existing session and auth integration tests updated for shared H2 context cleanup

## Verification

- `./gradlew.bat build` succeeds with 75 tests passing.
- Flyway V6 migration runs successfully.
- Registered users automatically receive a personal organization with OWNER role.
- Authenticated users can list, get current, create, and update organizations.
- Unauthenticated requests to org endpoints return 403 Forbidden.
- Non-members cannot access or update an organization (404/403).
- MEMBER role users cannot update organization metadata (403).
