package com.i2i.tenant.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i2i.tenant.constants.ApplicationConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class JWTTokenValidatorFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(ApplicationConstants.JWT_HEADER);

        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt;
        if (authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
        } else {
            jwt = authorizationHeader;
        }

        try {
            if (jwtSecret == null) {
                throw new IllegalStateException("JWT secret is not configured");
            }

            SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            String username = String.valueOf(claims.get("username"));
            String authorities = String.valueOf(claims.get("authorities"));

            if (username == null || authorities == null) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                    "Invalid token claims");
                return;
            }

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("JWT Token validation failed", e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, 
                "Invalid token: " + e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) 
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        return path.startsWith("/actuator") || 
               path.equals("/api/users/login") || 
               path.equals("/api/users/register");
    }
}
