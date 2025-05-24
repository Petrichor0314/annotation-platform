package com.mohamedoujdid.annotationplatform.admin.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mohamedoujdid.annotationplatform.admin.dto.user.UpdateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.user.CreateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.service.AdminUserService;
import com.mohamedoujdid.annotationplatform.exception.UserNotFoundException;
import com.mohamedoujdid.annotationplatform.notification.EmailService;
import com.mohamedoujdid.annotationplatform.user.model.Admin;
import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import com.mohamedoujdid.annotationplatform.user.model.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.AdminRepository;
import com.mohamedoujdid.annotationplatform.user.repository.AnnotatorRepository;
import com.mohamedoujdid.annotationplatform.user.repository.RoleRepository;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;
import com.mohamedoujdid.annotationplatform.utils.PasswordGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AnnotatorRepository annotatorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }
    
    @Override
    public List<Annotator> getAllAnnotators() {
        return annotatorRepository.findAllByDeletedFalse();
    }

    @Transactional
    @Override
    public void createUser(CreateUserRequest form) {
        // Always use ANNOTATOR role regardless of what's in the form
        Role role = roleRepository.findByName("ANNOTATOR")
                .orElseThrow(() -> new IllegalArgumentException("Annotator role not found"));
        
        // Generate a random password using the existing PasswordGenerator utility
        String rawPassword = PasswordGenerator.generateSecurePassword(12);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // Create annotator
        Annotator annotator = new Annotator();
        annotator.setFirstName(form.getFirstName());
        annotator.setLastName(form.getLastName());
        annotator.setEmail(form.getEmail());
        annotator.setPassword(encodedPassword);
        annotator.setRole(role);
        annotator.setAccountNonLocked(true);
        annotator.setPasswordChangeRequired(true);
        annotator.setDeleted(false);
        User savedUser = annotatorRepository.save(annotator);
        
        // Send the password to the user's email
        try {
            emailService.sendTemporaryCredentials(form.getEmail(), rawPassword);
            log.info("Temporary credentials sent to {}", form.getEmail());
        } catch (Exception e) {
            log.error("Failed to send credentials email to {}: {}", form.getEmail(), e.getMessage());
            // We don't want to roll back the transaction if email sending fails
            // The user is still created, but the admin will need to reset the password manually
        }
    }

    @Override
    public UpdateUserRequest getUpdateUserForm(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        // Check if the user is an admin - if so, throw an exception
        if (user instanceof Admin) {
            throw new IllegalArgumentException("Admin users cannot be edited by other admins");
        }

        return UpdateUserRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roleId(user.getRole().getId())
                .accountNonLocked(user.isAccountNonLocked())
                .passwordChangeRequired(user.isPasswordChangeRequired())
                .build();
    }

    @Transactional
    @Override
    public void updateUser(Long id, UpdateUserRequest form, Role newRole) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found or already deleted"));

        // Check if the user is an admin - if so, throw an exception
        if (existingUser instanceof Admin) {
            throw new IllegalArgumentException("Admin users cannot be edited by other admins");
        }

        // Just update the basic user information without changing the role
        existingUser.setFirstName(form.getFirstName());
        existingUser.setLastName(form.getLastName());
        existingUser.setEmail(form.getEmail());
        existingUser.setAccountNonLocked(form.isAccountNonLocked());
        existingUser.setPasswordChangeRequired(form.isPasswordChangeRequired());
        
        // Save the user based on its type
        if (existingUser instanceof Annotator) {
            annotatorRepository.save((Annotator) existingUser);
        } else {
            userRepository.save(existingUser);
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        // Check if the user is an admin - if so, throw an exception
        if (user instanceof Admin) {
            throw new IllegalArgumentException("Admin users cannot be deleted by other admins");
        }

        if (!user.isDeleted()) {
            user.setDeleted(true);
            userRepository.save(user);
        }
    }
}
