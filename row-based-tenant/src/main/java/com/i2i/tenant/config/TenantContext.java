package com.i2i.tenant.config;

public class TenantContext {
    private static final ThreadLocal<Long> currentOrganization = new ThreadLocal<>();

    public static void setOrganization(Long organizationId) {
        currentOrganization.set(organizationId);
    }

    public static Long getOrganization() {
        return currentOrganization.get();
    }

    public static void clear() {
        currentOrganization.remove();
    }
}
