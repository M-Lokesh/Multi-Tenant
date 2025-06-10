package com.i2i.tenant.security;

import org.springframework.stereotype.Component;

@Component
public class OrganizationContext {
    private static final ThreadLocal<Long> currentOrganization = new ThreadLocal<>();

    public void setCurrentOrganizationId(Long organizationId) {
        currentOrganization.set(organizationId);
    }

    public Long getCurrentOrganizationId() {
        Long organizationId = currentOrganization.get();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        return organizationId;
    }

    public void clear() {
        currentOrganization.remove();
    }
} 