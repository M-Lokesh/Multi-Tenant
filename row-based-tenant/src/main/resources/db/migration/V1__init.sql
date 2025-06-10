---- Ensure we're in the public schema
--SET search_path TO public;

-- Create organizations table first
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT chk_org_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Create index on organization code
CREATE INDEX IF NOT EXISTS idx_org_code ON organizations(code);

-- Insert default organization
INSERT INTO organizations (code, name, description, status, created_by)
VALUES ('public', 'Public Organization', 'Default organization for system-wide operations', 'ACTIVE', 'SYSTEM')
ON CONFLICT (code) DO NOTHING;

-- Create roles table (master table)
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    organization_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED')),
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Create user_roles table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'SYSTEM',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Create product table
CREATE TABLE IF NOT EXISTS product (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    price DOUBLE PRECISION,
    organization_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT fk_product_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_user_status ON users(status);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_org_id ON users(organization_id);
CREATE INDEX IF NOT EXISTS idx_product_org_id ON product(organization_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_org_id ON user_roles(organization_id);

-- Insert default roles
INSERT INTO roles (name, description, created_by)
VALUES
    ('ROLE_USER', 'Regular user role', 'SYSTEM'),
    ('ROLE_ADMIN', 'Administrator role', 'SYSTEM')
ON CONFLICT (name) DO NOTHING;

-- Insert default admin user
INSERT INTO users (username, email, password, mobile_number, status, organization_id, created_by)
VALUES (
    'admin',
    'admin@example.com',
    '{bcrypt}$2a$12$88.f6upbBvy0okEa7OfHFuorV29qeK.sVbB9VQ6J6dWM1bW6Qef8m',
    '1234567890',
    'ACTIVE',
    1,
    'SYSTEM'
)
ON CONFLICT (username) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id, organization_id, created_by)
SELECT u.id, r.id, 1, 'SYSTEM'
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT (user_id, role_id) DO NOTHING;
