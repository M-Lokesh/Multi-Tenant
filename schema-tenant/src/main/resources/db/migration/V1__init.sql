-- Create users table
-- users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    mobile_number VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    CONSTRAINT chk_user_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);


-- Create roles table
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);


-- Create user_roles table for many-to-many relationship
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) DEFAULT 'SYSTEM',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_user_status ON users(status);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_username ON users(username);

-- Insert default roles
INSERT INTO roles (name, description, created_by)
VALUES
    ('ROLE_USER', 'Regular user role', 'SYSTEM'),
    ('ROLE_ADMIN', 'Administrator role', 'SYSTEM');

-- Insert default admin user
INSERT INTO users (username, email, password, mobile_number, status, created_by)
VALUES (
    'admin',
    'admin@example.com',
    '{bcrypt}$2a$12$88.f6upbBvy0okEa7OfHFuorV29qeK.sVbB9VQ6J6dWM1bW6Qef8m',
    '1234567890',
    'ACTIVE',
    'SYSTEM'
);

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id, created_by)
SELECT u.id, r.id, 'SYSTEM'
FROM users u
CROSS JOIN roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- product table
CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    price DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);
