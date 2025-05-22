package com.mohamedoujdid.annotationplatform.admin.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateUserRequest {

    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotNull
    private Long roleId;

    private boolean accountNonLocked;

    private boolean passwordChangeRequired;
}
