package com.mohamedoujdid.annotationplatform.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
public class CreateUserRequest {
    @Email
    @NotEmpty
    private String email;
}