package com.i2i.tenant.repository;

import com.i2i.tenant.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.organization.id = :organizationId")
    Optional<User> findByEmailAndOrganizationId(@Param("email") String email, @Param("organizationId") Long organizationId);
    
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.organization.id = :organizationId")
    Optional<User> findByUsernameAndOrganizationId(@Param("username") String username, @Param("organizationId") Long organizationId);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email AND u.organization.id = :organizationId")
    boolean existsByEmailAndOrganizationId(@Param("email") String email, @Param("organizationId") Long organizationId);
    
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.username = :username AND u.organization.id = :organizationId")
    boolean existsByUsernameAndOrganizationId(@Param("username") String username, @Param("organizationId") Long organizationId);
    
    // Keep the original methods for backward compatibility
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
} 