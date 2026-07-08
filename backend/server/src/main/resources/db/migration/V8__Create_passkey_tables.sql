CREATE TABLE passkeys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    credential_id VARCHAR(1024) NOT NULL,
    public_key BYTEA NOT NULL,
    counter BIGINT NOT NULL DEFAULT 0,
    credential_type VARCHAR(50) NOT NULL DEFAULT 'public-key',
    aaguid VARCHAR(36),
    device_name VARCHAR(255),
    backed_up BOOLEAN NOT NULL DEFAULT false,
    transports VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE webauthn_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    challenge VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    purpose VARCHAR(20) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX idx_passkeys_credential_id ON passkeys (credential_id);
CREATE INDEX idx_passkeys_user_id ON passkeys (user_id);
CREATE INDEX idx_webauthn_challenges_challenge ON webauthn_challenges (challenge);
CREATE INDEX idx_webauthn_challenges_user_id ON webauthn_challenges (user_id);
