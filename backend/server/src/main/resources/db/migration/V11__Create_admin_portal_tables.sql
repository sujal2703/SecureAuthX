CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    ip_address VARCHAR(45),
    action VARCHAR(100) NOT NULL,
    target VARCHAR(500),
    success BOOLEAN NOT NULL DEFAULT true,
    details TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE system_announcements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    active BOOLEAN NOT NULL DEFAULT true,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE system_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE security_incidents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    incident_type VARCHAR(100) NOT NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    description TEXT,
    ip_address VARCHAR(45),
    resolved BOOLEAN NOT NULL DEFAULT false,
    resolved_by UUID REFERENCES users(id) ON DELETE SET NULL,
    resolved_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs (action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_system_announcements_active ON system_announcements (active);
CREATE INDEX idx_system_settings_key ON system_settings (setting_key);
CREATE INDEX idx_security_incidents_user_id ON security_incidents (user_id);
CREATE INDEX idx_security_incidents_type ON security_incidents (incident_type);
CREATE INDEX idx_security_incidents_resolved ON security_incidents (resolved);

INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('maintenance_mode', 'false', 'When enabled, the platform returns maintenance mode to all requests.'),
('registration_enabled', 'true', 'When disabled, new user registration is blocked.'),
('max_sessions_per_user', '10', 'Maximum number of active sessions per user.'),
('password_policy_level', 'standard', 'Password policy level: standard, strict, or minimal.');
