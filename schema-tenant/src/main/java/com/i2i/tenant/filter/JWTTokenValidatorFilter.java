package com.i2i.tenant.filter;

import com.i2i.tenant.config.TenantContext;
import com.i2i.tenant.constants.ApplicationConstants;
import com.i2i.tenant.exceptionalhandling.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;

@Component
//@Order(2)
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
       // String tenantId = request.getHeader(TENANT_HEADER);

        if (authorizationHeader != null) {
            String jwt;

            // Support both "Bearer <token>" and "<token>"
            if (authorizationHeader.startsWith("Bearer ")) {
                jwt = authorizationHeader.substring(7);
            } else {
                jwt = authorizationHeader;
            }

            try {
                Environment env = getEnvironment();
                if (env != null) {
                    String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
                            ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
                    SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                    if (secretKey != null) {
                        Claims claims = Jwts.parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(jwt)
                                .getPayload();

                        String username = String.valueOf(claims.get("username"));
                        String authorities = String.valueOf(claims.get("authorities"));

//                        if (tenantId != null && !tenantId.isBlank()) {
//                            TenantContext.setTenant(tenantId);
//                        }

                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

                        SecurityContextHolder.getContext().setAuthentication(authentication);

                    }
                }
            } catch (Exception exception) {
                //custom
               // throw new ApiException("Invalid JWT Token", HttpServletResponse.SC_UNAUTHORIZED);

                throw new BadCredentialsException("Invalid Token received!");
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        boolean shouldSkip = path.startsWith("/actuator") || path.startsWith("/apiLogin");
        System.out.println("Skipping filter for path in auth: " + path + " -> " + shouldSkip);
        return shouldSkip;
    }


}
