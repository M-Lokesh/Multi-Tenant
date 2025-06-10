package com.i2i.tenant.config;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class TenantInterceptor extends EmptyInterceptor {

    public void enableOrganizationFilter(Session session) {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId != null) {
            session.enableFilter("organizationFilter")
                  .setParameter("organizationId", organizationId);
        }
    }
} 