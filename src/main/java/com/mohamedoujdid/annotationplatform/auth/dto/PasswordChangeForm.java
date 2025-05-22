package com.mohamedoujdid.annotationplatform.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;

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

    @AssertTrue(message = "New password and confirmation must match")
    public boolean isPasswordMatching() {
        if (newPassword == null || confirmPassword == null) return false;
        return newPassword.equals(confirmPassword);
    }
}
