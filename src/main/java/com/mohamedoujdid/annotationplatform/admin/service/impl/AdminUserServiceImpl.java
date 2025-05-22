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
import com.mohamedoujdid.annotationplatform.user.model.Admin;
import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import com.mohamedoujdid.annotationplatform.user.model.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.AdminRepository;
import com.mohamedoujdid.annotationplatform.user.repository.AnnotatorRepository;
import com.mohamedoujdid.annotationplatform.user.repository.RoleRepository;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final AnnotatorRepository annotatorRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllByDeletedFalse();
    }

    @Transactional
    @Override
    public void createUser(CreateUserRequest form) {
        Role role = roleRepository.findById(form.getRole().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid Role ID: " + form.getRole().getId()));

        String roleName = role.getName() != null ? role.getName().toUpperCase() : "";
        String encodedPassword = passwordEncoder.encode("password"); // Default password

        if ("ADMIN".equals(roleName)) {
            Admin admin = new Admin();
            admin.setFirstName(form.getFirstName());
            admin.setLastName(form.getLastName());
            admin.setEmail(form.getEmail());
            admin.setPassword(encodedPassword);
            admin.setRole(role);
            admin.setAccountNonLocked(true);
            admin.setPasswordChangeRequired(true);
            admin.setDeleted(false);
            adminRepository.save(admin);
        } else if ("ANNOTATOR".equals(roleName)) {
            Annotator annotator = new Annotator();
            annotator.setFirstName(form.getFirstName());
            annotator.setLastName(form.getLastName());
            annotator.setEmail(form.getEmail());
            annotator.setPassword(encodedPassword);
            annotator.setRole(role);
            annotator.setAccountNonLocked(true);
            annotator.setPasswordChangeRequired(true);
            annotator.setDeleted(false);
            annotatorRepository.save(annotator);
        } else {
            User genericUser = new User();
            genericUser.setFirstName(form.getFirstName());
            genericUser.setLastName(form.getLastName());
            genericUser.setEmail(form.getEmail());
            genericUser.setPassword(encodedPassword);
            genericUser.setRole(role);
            genericUser.setAccountNonLocked(true);
            genericUser.setPasswordChangeRequired(true);
            genericUser.setDeleted(false);
            userRepository.save(genericUser);
        }
    }

    @Override
    public UpdateUserRequest getUpdateUserForm(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

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

        Role currentRole = existingUser.getRole();
        String newRoleName = newRole.getName() != null ? newRole.getName().toUpperCase() : "";

        if (!Objects.equals(currentRole.getId(), newRole.getId())) {
            String email = form.getEmail();
            String passwordHash = existingUser.getPassword();
            String firstName = form.getFirstName();
            String lastName = form.getLastName();
            boolean accountNonLocked = form.isAccountNonLocked();
            boolean passwordChangeRequired = form.isPasswordChangeRequired();

            userRepository.delete(existingUser);
            userRepository.flush();

            if ("ADMIN".equals(newRoleName)) {
                Admin admin = new Admin();
                admin.setFirstName(firstName);
                admin.setLastName(lastName);
                admin.setEmail(email);
                admin.setPassword(passwordHash);
                admin.setRole(newRole);
                admin.setAccountNonLocked(accountNonLocked);
                admin.setPasswordChangeRequired(passwordChangeRequired);
                admin.setDeleted(false);
                adminRepository.save(admin);
            } else if ("ANNOTATOR".equals(newRoleName)) {
                Annotator annotator = new Annotator();
                annotator.setFirstName(firstName);
                annotator.setLastName(lastName);
                annotator.setEmail(email);
                annotator.setPassword(passwordHash);
                annotator.setRole(newRole);
                annotator.setAccountNonLocked(accountNonLocked);
                annotator.setPasswordChangeRequired(passwordChangeRequired);
                annotator.setDeleted(false);
                annotatorRepository.save(annotator);
            } else {
                User genericUser = new User();
                genericUser.setFirstName(firstName);
                genericUser.setLastName(lastName);
                genericUser.setEmail(email);
                genericUser.setPassword(passwordHash);
                genericUser.setRole(newRole);
                genericUser.setAccountNonLocked(accountNonLocked);
                genericUser.setPasswordChangeRequired(passwordChangeRequired);
                genericUser.setDeleted(false);
                userRepository.save(genericUser);
            }
        } else {
            existingUser.setFirstName(form.getFirstName());
            existingUser.setLastName(form.getLastName());
            existingUser.setEmail(form.getEmail());
            existingUser.setAccountNonLocked(form.isAccountNonLocked());
            existingUser.setPasswordChangeRequired(form.isPasswordChangeRequired());
            if (existingUser instanceof Admin) {
                adminRepository.save((Admin) existingUser);
            } else if (existingUser instanceof Annotator) {
                annotatorRepository.save((Annotator) existingUser);
            } else {
                userRepository.save(existingUser);
            }
        }
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + id + " not found"));

        if (!user.isDeleted()) {
            user.setDeleted(true);
            userRepository.save(user);
        }
    }
}

