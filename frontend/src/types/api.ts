export interface Session {
  id: string;
  createdAt: string;
  lastActivityAt: string;
  expiresAt: string;
  revoked: boolean;
  expired: boolean;
  ipAddress: string;
  deviceName: string;
  operatingSystem: string;
  browser: string;
  current: boolean;
}

export interface Passkey {
  id: string;
  credentialId: string;
  deviceName: string;
  aaguid: string;
  credentialType: string;
  backedUp: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Organization {
  id: string;
  name: string;
  slug: string;
  personal: boolean;
  role: string;
  createdAt: string;
}

export interface UserInfoResponse {
  sub: string;
  email: string;
  given_name?: string;
  family_name?: string;
}

export interface OAuthClient {
  id: string;
  clientId: string;
  clientName: string;
  confidential: boolean;
  enabled: boolean;
  redirectUris: string[];
  createdAt: string;
}

export interface OAuthClientCreateRequest {
  clientId: string;
  clientSecret?: string;
  clientName: string;
  confidential: boolean;
  redirectUris: string[];
}

export interface OAuthClientCreateResponse extends OAuthClient {
  clientSecret: string;
}

export interface OrganizationCreateRequest {
  name: string;
}

export interface OrganizationUpdateRequest {
  name: string;
}

// Developer Portal - Projects
export interface ProjectResponse {
  id: string;
  userId: string;
  name: string;
  description: string;
  oauthClientId: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface ProjectCreateRequest {
  name: string;
  description?: string;
  oauthClientId?: string;
}

export interface ProjectUpdateRequest {
  name?: string;
  description?: string;
}

// Developer Portal - API Keys
export interface ApiKeyResponse {
  id: string;
  projectId: string;
  keyPrefix: string;
  label: string;
  lastUsedAt: string | null;
  expiresAt: string | null;
  enabled: boolean;
  createdAt: string;
}

export interface CreateApiKeyRequest {
  label: string;
  expiresAt?: string;
}

export interface CreateApiKeyResponse {
  id: string;
  projectId: string;
  keyPrefix: string;
  plainTextKey: string;
  label: string;
  expiresAt: string | null;
  createdAt: string;
}

// Developer Portal - Rate Limits
export interface RateLimitRequest {
  requestsPerMinute: number;
  requestsPerHour: number;
}

export interface RateLimitResponse {
  id: string;
  projectId: string;
  requestsPerMinute: number;
  requestsPerHour: number;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
}

// Developer Portal - Usage
export interface UsageAnalyticsResponse {
  id: string;
  projectId: string;
  date: string;
  requestCount: number;
  successCount: number;
  failureCount: number;
  avgLatencyMs: number;
  lastRequestAt: string;
  tokenExchanges: number;
  userinfoRequests: number;
}

// Developer Portal - Secret Rotation
export interface RotateSecretResponse {
  projectId: string;
  oauthClientId: string;
  newClientSecret: string;
  rotatedAt: string;
}

// Passkey Registration
export interface RegisterOptionsResponse {
  challenge: string;
  rp: { name: string; id: string };
  user: { id: string; name: string; displayName: string };
  pubKeyCredParams: Array<{ type: string; alg: number }>;
  authenticatorSelection: {
    residentKey: string;
    userVerification: string;
    requireResidentKey: boolean;
  };
  hints: string[];
  attestation: { fmt: string; alg: number };
}

export interface RegisterVerificationRequest {
  id: string;
  rawId: string;
  type: string;
  clientDataJSON: string;
  attestationObject: string;
  authenticatorData: string;
  publicKey: string;
  publicKeyAlgorithm: string;
  transports: string;
  aaguid: string;
  deviceName: string;
}

export interface RegisterVerificationResponse {
  id: string;
  verified: boolean;
  credentialId: string;
}

// Admin Dashboard
export interface DashboardResponse {
  totalUsers: number;
  totalOrganizations: number;
  totalOAuthClients: number;
  totalPasskeys: number;
  totalSessions: number;
  totalLoginEvents: number;
  activeSessions: number;
  securityIncidents: number;
  developerProjects: number;
}

// Admin Audit Logs
export interface AuditLogResponse {
  id: string;
  userId: string;
  organizationId: string;
  ipAddress: string;
  action: string;
  target: string;
  success: boolean;
  details: string;
  createdAt: string;
}

// Admin Security Incidents
export interface SecurityIncidentResponse {
  id: string;
  userId: string;
  incidentType: string;
  severity: string;
  description: string;
  ipAddress: string;
  resolved: boolean;
  resolvedBy: string | null;
  resolvedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface IncidentResolveRequest {
  resolved: boolean;
}

// Admin Announcements
export interface AnnouncementRequest {
  title: string;
  message: string;
  severity?: string;
  active?: boolean;
}

export interface AnnouncementResponse {
  id: string;
  title: string;
  message: string;
  severity: string;
  active: boolean;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

// Admin System Settings
export interface SystemSettingRequest {
  settingValue: string;
}

export interface SystemSettingResponse {
  id: string;
  settingKey: string;
  settingValue: string;
  description: string;
  createdAt: string;
  updatedAt: string;
}
