package com.i2i.tenant.config;

import com.i2i.tenant.exceptionalhandling.ApiException;
import com.i2i.tenant.service.TenantSchemaService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
//@Order(1) // Lower value => Higher precedence => executes first
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    private final TenantSchemaService tenantSchemaService;

    public TenantFilter(TenantSchemaService tenantSchemaService) {
        this.tenantSchemaService = tenantSchemaService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader("X-Tenant-ID");

        //send without global exception
        if (tenantId == null || tenantId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing tenant ID in request header\"}");
            return;
        }

        if (!tenantSchemaService.schemaExists(tenantId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Tenant schema '" + tenantId + "' does not exist\"}");
            return;
        }

//        //custom exception will not work here because spring security handle it
//        if (StringUtils.isEmpty(tenantId)) {
//            throw new ApiException("Missing tenant ID in request header", HttpServletResponse.SC_BAD_REQUEST);
//        }
//        if (!tenantSchemaService.schemaExists(tenantId)) {
//            throw new ApiException("Tenant schema '" + tenantId + "' does not exist", HttpServletResponse.SC_BAD_REQUEST);
//        }


        // Set tenant context here if needed
        TenantContext.setTenant(tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

}


