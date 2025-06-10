package com.i2i.tenant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(
    name = "organizationFilter",
    parameters = @ParamDef(name = "organizationId", type = Long.class)
)
@Filter(name = "organizationFilter", condition = "organization_id = :organizationId")
@Getter
@Setter
public abstract class TenantAwareEntity extends Auditable {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    public Long getOrganizationId() {
        return organization != null ? organization.getId() : null;
    }

    public void setOrganizationId(Long organizationId) {
        if (this.organization == null) {
            this.organization = new Organization();
        }
        this.organization.setId(organizationId);
    }
} 