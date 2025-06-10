-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_roles ENABLE ROW LEVEL SECURITY;
ALTER TABLE product ENABLE ROW LEVEL SECURITY;
ALTER TABLE organizations ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY users_organization_isolation ON users
    FOR ALL
    USING (organization_id = current_setting('app.current_organization_id')::bigint);

CREATE POLICY user_roles_organization_isolation ON user_roles
    FOR ALL
    USING (organization_id = current_setting('app.current_organization_id')::bigint);

CREATE POLICY product_organization_isolation ON product
    FOR ALL
    USING (organization_id = current_setting('app.current_organization_id')::bigint);

CREATE POLICY organizations_isolation ON organizations
    FOR ALL
    USING (id = current_setting('app.current_organization_id')::bigint);

-- Create function to set organization context
CREATE OR REPLACE FUNCTION set_organization_context(org_id bigint)
RETURNS void AS $$
BEGIN
    PERFORM set_config('app.current_organization_id', org_id::text, false);
END;
$$ LANGUAGE plpgsql;

-- Create audit log table
CREATE TABLE audit_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    details JSONB,
    ip_address VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_logs_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Create index on audit log table
CREATE INDEX idx_audit_logs_org_id ON audit_logs(organization_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Enable RLS on audit logs
ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;

-- Create RLS policy for audit logs
CREATE POLICY audit_logs_organization_policy ON audit_logs
    FOR ALL
    USING (organization_id = current_setting('app.current_organization_id')::bigint); 