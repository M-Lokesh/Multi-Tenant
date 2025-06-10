package com.i2i.tenant.config;

import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.springframework.stereotype.Component;

@Component
public class TenantSessionEventListener implements PostLoadEventListener {

    private final TenantInterceptor tenantInterceptor;

    public TenantSessionEventListener(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        tenantInterceptor.enableOrganizationFilter(event.getSession());
    }
} 