package com.i2i.tenant.repository;

import com.i2i.tenant.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    @Query("SELECT p FROM Product p WHERE p.organization.id = :organizationId")
    List<Product> findAllByOrganizationId(@Param("organizationId") Long organizationId);
    
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.organization.id = :organizationId")
    Optional<Product> findByIdAndOrganizationId(@Param("id") Long id, @Param("organizationId") Long organizationId);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.id = :id AND p.organization.id = :organizationId")
    boolean existsByIdAndOrganizationId(@Param("id") Long id, @Param("organizationId") Long organizationId);
}
