package com.i2i.tenant.filter;

import com.i2i.tenant.config.TenantContext;
import com.i2i.tenant.constants.ApplicationConstants;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;

public class JWTTokenGeneratorFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null != authentication) {
            Environment env = getEnvironment();
            if (null != env) {
                String tenantId = request.getHeader(TENANT_HEADER);

                if (tenantId != null && !tenantId.isBlank()) {
                    //uncomment this when we create schema automatically
                    //tenantSchemaService.ensureSchemaExists(tenantId);
                    TenantContext.setTenant(tenantId);
                }
                String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                        ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                String jwt = Jwts.builder().issuer("Bank").subject("JWT Token")
                        .claim("username", authentication.getName())
                        .claim("authorities", authentication.getAuthorities().stream().map(
                                GrantedAuthority::getAuthority).collect(Collectors.joining(",")))
                        //.claim("tenant", TenantContext.getTenant()) // Add tenant
                        .issuedAt(new Date())
                        .expiration(new Date((new Date()).getTime() + 30000000))
                        .signWith(secretKey).compact();
                response.setHeader(ApplicationConstants.JWT_HEADER, jwt);
            }
        }
        filterChain.doFilter(request, response);
    }

//    @Override
//    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
//        return !request.getServletPath().equals("/user");
//    }

}
