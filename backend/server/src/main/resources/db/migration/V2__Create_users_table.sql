CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT chk_users_email_not_blank CHECK (length(trim(email)) > 0),
    CONSTRAINT chk_users_email_normalized CHECK (email = lower(email)),
    CONSTRAINT chk_users_password_hash_not_blank CHECK (length(trim(password_hash)) > 0)
);

CREATE INDEX idx_users_created_at ON users (created_at);
