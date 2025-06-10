package com.i2i.tenant.service;

import com.i2i.tenant.model.Role;
import com.i2i.tenant.model.User;
import com.i2i.tenant.repository.RoleRepository;
import com.i2i.tenant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set default role if no roles are provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            roleRepository.findByName("ROLE_USER").ifPresent(roles::add);
            user.setRoles(roles);
        }

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

//    @Transactional
//    public User updateUser(User user) {
//        if (!userRepository.existsById(user.getId())) {
//            throw new RuntimeException("User not found");
//        }
//
//        // If password is being updated, encode it
//        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
//            user.setPassword(passwordEncoder.encode(user.getPassword()));
//        }
//
//        return userRepository.save(user);
//    }

    @Transactional
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Update only the fields that are provided
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getMobileNumber() != null) {
            existingUser.setMobileNumber(user.getMobileNumber());
        }
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public User addRoleToUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRoleFromUser(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().remove(role);
        return userRepository.save(user);
    }
} 