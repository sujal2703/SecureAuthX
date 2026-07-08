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
