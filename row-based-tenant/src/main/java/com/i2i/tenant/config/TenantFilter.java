package com.i2i.tenant.config;

import com.i2i.tenant.model.Organization;
import com.i2i.tenant.repository.OrganizationRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String ORGANIZATION_HEADER = "X-Organization-Code";
    private final OrganizationRepository organizationRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String organizationCode = request.getHeader(ORGANIZATION_HEADER);

        if (organizationCode != null && !organizationCode.isBlank()) {
            organizationRepository.findByCode(organizationCode)
                .ifPresent(org -> TenantContext.setOrganization(org.getId()));
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

