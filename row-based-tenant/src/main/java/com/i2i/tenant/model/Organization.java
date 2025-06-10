package com.i2i.tenant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organizations")
@Getter
@Setter
public class Organization extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;  // This will be used as tenant_id

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    public enum OrganizationStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
} 