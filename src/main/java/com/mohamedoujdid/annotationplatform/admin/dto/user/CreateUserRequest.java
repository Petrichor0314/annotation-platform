package com.mohamedoujdid.annotationplatform.admin.dto.user;

import com.mohamedoujdid.annotationplatform.user.model.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

    private Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    // Role is no longer required in the form as it will be set automatically to ANNOTATOR
    private Role role;
}