# Sprint F2 — Dashboard & User Experience (Frontend)

Status: COMPLETED on 2026-07-08

## Objective

Transform the authentication frontend into a usable authenticated application with dashboard, profile, session management, device management, and improved navigation.

## Scope Implemented

### 1. Dashboard
- Welcome section with user greeting
- User information display (email, name, sessions, devices)
- Quick statistics cards (Account Status, Active Sessions, Registered Devices, Security Status)
- User information card with session/device counts
- Quick action buttons linking to Profile, Sessions, Devices
- Security status indicator

### 2. Profile Page
- Account details (email, name, role, user ID)
- Organization information (name, role, creation date)
- Integration with OIDC UserInfo endpoint for user profile data
- Integration with Organizations API for organization data

### 3. Sessions Management
- List all active sessions with device information
- Current session badge indicator
- Device details (browser, OS, IP address, device name)
- Session timestamps (created at, last activity)
- Revoke individual session
- Revoke all other sessions
- Empty state when no sessions exist
- Toast notifications for success/failure

### 4. Devices Management
- List registered passkeys/WebAuthn devices
- Device details (name, type, backup status, registration date)
- Remove device with confirmation
- Empty state for no registered devices
- Toast notifications for success/failure

### 5. Settings Page
- Placeholder page with settings categories
- Coming soon indicators for future settings

### 6. Navigation
- Updated sidebar with: Dashboard, Profile, Sessions, Devices, Settings
- Active route highlighting
- Removed old placeholder nav items (Users, Security, Activity)
- Header now displays dynamic page title and user email
- Profile icon in header links to profile page

### 7. API Integration
- OIDC UserInfo endpoint (`GET /connect/userinfo`) for user profile
- Sessions endpoints:
  - `GET /api/v1/sessions` — list sessions
  - `DELETE /api/v1/sessions/{id}` — revoke session
  - `DELETE /api/v1/sessions/all` — revoke all
- Passkeys endpoints:
  - `GET /api/v1/passkeys` — list devices
  - `DELETE /api/v1/passkeys/{id}` — remove device
- Organizations endpoint:
  - `GET /api/v1/organizations/current` — get organization

### 8. Authentication Context
- Added `fetchProfile` to fetch user info via OIDC UserInfo
- User profile is fetched after login, register, and token refresh
- Graceful fallback if profile fetch fails

### 9. Testing
- Dashboard tests: renders welcome, stat cards, quick actions, user info
- Profile tests: renders page, account details, organization, email display
- Sessions tests: renders page, session cards, empty state, current badge
- Navigation tests: renders all items, brand, sign out, active route highlight
- Updated auth-context tests with profile service mock

### 10. UI/UX
- Consistent use of shadcn/ui components (Card, Button, Skeleton, Alert, Toast)
- Lucide React icons throughout
- Loading states with Skeleton components
- Error states with Alert components
- Empty states with descriptive messages
- Toast notifications for actions
- Responsive grid layouts

## API Integrations Used

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/connect/userinfo` | GET | Fetch user profile |
| `/api/v1/sessions` | GET | List active sessions |
| `/api/v1/sessions/{id}` | DELETE | Revoke session |
| `/api/v1/sessions/all` | DELETE | Revoke all sessions |
| `/api/v1/passkeys` | GET | List registered devices |
| `/api/v1/passkeys/{id}` | DELETE | Remove device |
| `/api/v1/organizations/current` | GET | Get organization info |

## Files Created

### Frontend Source
- `frontend/src/types/api.ts` — API response types (Session, Passkey, Organization, UserInfo)
- `frontend/src/services/profile-service.ts` — Profile/OIDC service
- `frontend/src/services/session-service.ts` — Session management service
- `frontend/src/services/passkey-service.ts` — Passkey/device service
- `frontend/src/services/organization-service.ts` — Organization service
- `frontend/src/app/dashboard/profile/page.tsx` — Profile page
- `frontend/src/app/dashboard/sessions/page.tsx` — Sessions page
- `frontend/src/app/dashboard/devices/page.tsx` — Devices page
- `frontend/src/app/dashboard/settings/page.tsx` — Settings placeholder

### Tests
- `frontend/__tests__/dashboard.test.tsx` — Dashboard tests (4)
- `frontend/__tests__/profile.test.tsx` — Profile tests (4)
- `frontend/__tests__/sessions.test.tsx` — Sessions tests (4)
- `frontend/__tests__/navigation.test.tsx` — Navigation tests (4)

## Files Modified
- `frontend/src/contexts/auth-context.tsx` — Added fetchProfile, profile integration
- `frontend/src/components/layout/sidebar.tsx` — Updated navigation items
- `frontend/src/components/layout/header.tsx` — Dynamic title, user display, profile link
- `frontend/src/app/dashboard/page.tsx` — Rewritten with real user data
- `frontend/__tests__/auth-context.test.tsx` — Added profile service mock
- `README.md` — Updated with frontend info
- `IMPLEMENTATION_PLAN.md` — Added Sprint F2
- `.ai/12_CURRENT_SPRINT.md` — Updated
- `.ai/13_PROJECT_MEMORY.md` — Updated

## Verification
- `npm run build` succeeds
- `npm run lint` passes
- `npm test` — all 20 frontend tests pass
- No backend changes required
- No placeholder code or TODO comments remain

## Architecture Decisions

### Profile via OIDC UserInfo
- No dedicated `/api/v1/users/me` endpoint exists
- OIDC UserInfo endpoint (`/connect/userinfo`) provides user profile data
- Given_name/family_name may be null if backend user entity lacks name fields

### React Query Usage
- Sessions and devices pages use manual state management (useState + useEffect)
- Consistent with existing dashboard pattern
- React Query remains available for future pages requiring caching/refetching

### Non-blocking Profile Fetch
- Profile fetching via OIDC fails silently
- User is considered authenticated even if profile fetch fails
- Profile data populates when available
