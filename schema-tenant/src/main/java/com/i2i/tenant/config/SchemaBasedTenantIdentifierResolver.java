package com.i2i.tenant.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

public class SchemaBasedTenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    public static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenant();
        return (tenantId != null) ? tenantId : DEFAULT_SCHEMA;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
