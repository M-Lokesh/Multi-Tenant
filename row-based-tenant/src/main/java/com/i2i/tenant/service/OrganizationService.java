package com.i2i.tenant.service;

import com.i2i.tenant.model.Organization;
import com.i2i.tenant.model.Role;
import com.i2i.tenant.repository.OrganizationRepository;
import com.i2i.tenant.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public Organization createOrganization(Organization organization) {
        // Validate organization code
        if (organization.getCode() == null || organization.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Organization code is required");
        }

        // Check if organization code already exists
        if (organizationRepository.existsByCode(organization.getCode())) {
            throw new IllegalArgumentException("Organization code already exists: " + organization.getCode());
        }

        // Set default status if not provided
        if (organization.getStatus() == null) {
            organization.setStatus(Organization.OrganizationStatus.ACTIVE);
        }

        Organization savedOrg = organizationRepository.save(organization);
        
        // Create default roles for the new organization
       // createDefaultRoles(savedOrg);
        
        return savedOrg;
    }

    @Transactional(readOnly = true)
    public List<Organization> getAllOrganizations() {
        return organizationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Organization> getOrganizationById(Long id) {
        return organizationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Organization> getOrganizationByCode(String code) {
        return organizationRepository.findByCode(code);
    }

    @Transactional
    public Organization updateOrganization(Long id, Organization organization) {
        Organization existingOrg = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));

        // Update only the fields that are provided
        if (organization.getName() != null) {
            existingOrg.setName(organization.getName());
        }
        if (organization.getDescription() != null) {
            existingOrg.setDescription(organization.getDescription());
        }
        if (organization.getStatus() != null) {
            existingOrg.setStatus(organization.getStatus());
        }

        // Code cannot be updated as it's used as a unique identifier
        return organizationRepository.save(existingOrg);
    }

    @Transactional
    public void deleteOrganization(Long id) {
        if (!organizationRepository.existsById(id)) {
            throw new IllegalArgumentException("Organization not found with id: " + id);
        }
        organizationRepository.deleteById(id);
    }

    @Transactional
    public Organization updateOrganizationStatus(Long id, Organization.OrganizationStatus status) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with id: " + id));
        
        organization.setStatus(status);
        return organizationRepository.save(organization);
    }

    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return organizationRepository.existsByCode(code);
    }
} 