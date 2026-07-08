CREATE TABLE oauth_clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(100) NOT NULL,
    client_secret VARCHAR(255),
    client_name VARCHAR(255) NOT NULL,
    confidential BOOLEAN NOT NULL DEFAULT false,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE oauth_client_redirect_uris (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL REFERENCES oauth_clients(id) ON DELETE CASCADE,
    redirect_uri VARCHAR(2048) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE oauth_authorization_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    client_id UUID NOT NULL REFERENCES oauth_clients(id) ON DELETE CASCADE,
    redirect_uri VARCHAR(2048) NOT NULL,
    code_challenge VARCHAR(255) NOT NULL,
    challenge_method VARCHAR(10) NOT NULL DEFAULT 'S256',
    expires_at TIMESTAMPTZ NOT NULL,
    consumed BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_oauth_clients_client_id ON oauth_clients (client_id);
CREATE INDEX idx_oauth_client_redirect_uris_client_id ON oauth_client_redirect_uris (client_id);
CREATE UNIQUE INDEX idx_oauth_authorization_codes_code ON oauth_authorization_codes (code);
CREATE INDEX idx_oauth_authorization_codes_user_id ON oauth_authorization_codes (user_id);
CREATE INDEX idx_oauth_authorization_codes_client_id ON oauth_authorization_codes (client_id);
