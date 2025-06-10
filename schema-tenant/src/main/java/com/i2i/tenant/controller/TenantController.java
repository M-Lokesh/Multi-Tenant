package com.i2i.tenant.controller;

import com.i2i.tenant.service.TenantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    @Autowired
    private TenantService tenantService;

    @PostMapping
    public ResponseEntity<String> createTenant(@RequestParam String tenantId) {
        tenantService.createTenant(tenantId);
        return ResponseEntity.ok("Tenant '" + tenantId + "' created successfully!");
    }
}
