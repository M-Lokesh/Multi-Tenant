package com.i2i.tenant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i2i.tenant.config.TenantContext;
import com.i2i.tenant.model.AuditLog;
import com.i2i.tenant.model.User;
import com.i2i.tenant.repository.AuditLogRepository;
import com.i2i.tenant.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String action, String entityType, Long entityId, String details) {
        try {
            AuditLog log = new AuditLog();
            log.setOrganizationId(TenantContext.getOrganization());
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            
            // Create a proper JSON structure for details
            Map<String, Object> detailsMap = new HashMap<>();
            detailsMap.put("message", details);
            detailsMap.put("timestamp", LocalDateTime.now().toString());
            log.setDetails(objectMapper.writeValueAsString(detailsMap));
            
            log.setCreatedAt(LocalDateTime.now());

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                userRepository.findByUsername(auth.getName())
                    .ifPresent(user -> log.setUserId(user.getId()));
            }

            // Get IP address
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                log.setIpAddress(getClientIp(request));
            }

            auditLogRepository.save(log);
        } catch (Exception e) {
            // Log the error but don't throw it to prevent disrupting the main operation
            System.err.println("Error creating audit log: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return request.getRemoteAddr();
    }
} 