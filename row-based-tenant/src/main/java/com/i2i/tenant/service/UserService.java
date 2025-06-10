package com.i2i.tenant.service;

import com.i2i.tenant.model.Role;
import com.i2i.tenant.model.User;
import com.i2i.tenant.model.UserStatus;
import com.i2i.tenant.repository.RoleRepository;
import com.i2i.tenant.repository.UserRepository;
import com.i2i.tenant.security.OrganizationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationContext organizationContext;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder, OrganizationContext organizationContext) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationContext = organizationContext;
    }

    public User createUser(User user) {
        // Set organization from context
        user.setOrganizationId(organizationContext.getCurrentOrganizationId());
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set default status if not provided
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }
        
        // Set audit fields
        user.setCreatedBy("SYSTEM");
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Assign default ROLE_USER
        Role defaultRole = roleRepository.findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));
        addRoleToUser(savedUser.getId(), defaultRole.getName());
        
        return savedUser;
    }

    public User addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found"));
            
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
            
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new RuntimeException("Role not found"));
            
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only the fields that are provided
        if (user.getUsername() != null) existingUser.setUsername(user.getUsername());
        if (user.getEmail() != null) existingUser.setEmail(user.getEmail());
        if (user.getMobileNumber() != null) existingUser.setMobileNumber(user.getMobileNumber());
        if (user.getStatus() != null) existingUser.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }
} 