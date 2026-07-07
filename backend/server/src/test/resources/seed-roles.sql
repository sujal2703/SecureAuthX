MERGE INTO roles (id, name, description, created_at, updated_at) KEY (name)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'USER', 'Standard user with basic permissions', NOW(), NOW());
MERGE INTO roles (id, name, description, created_at, updated_at) KEY (name)
VALUES ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'ADMIN', 'Administrator with elevated permissions', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a01', 'USER_READ', 'View user details', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a02', 'USER_WRITE', 'Create or update users', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a03', 'SESSION_READ', 'View session details', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a04', 'SESSION_REVOKE', 'Revoke sessions', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a05', 'ROLE_READ', 'View role details', NOW(), NOW());
MERGE INTO permissions (id, name, description, created_at, updated_at) KEY (name)
VALUES ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a06', 'ROLE_WRITE', 'Create or update roles', NOW(), NOW());

MERGE INTO role_permissions (id, role_id, permission_id, created_at) KEY (role_id, permission_id)
SELECT RANDOM_UUID(), r.id, p.id, NOW() FROM roles r, permissions p WHERE r.name = 'ADMIN';
MERGE INTO role_permissions (id, role_id, permission_id, created_at) KEY (role_id, permission_id)
SELECT RANDOM_UUID(), r.id, p.id, NOW() FROM roles r, permissions p WHERE r.name = 'USER' AND p.name IN ('SESSION_READ', 'SESSION_REVOKE', 'ROLE_READ');
