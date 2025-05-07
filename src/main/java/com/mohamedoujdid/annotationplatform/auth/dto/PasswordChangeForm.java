package com.mohamedoujdid.annotationplatform.auth.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeForm {
    @NotEmpty
    private String currentPassword;

    @NotEmpty
    @Size(min = 8)
    private String newPassword;

    @NotEmpty
    @Size(min = 8)
    private String confirmPassword;
}
