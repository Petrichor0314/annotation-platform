package com.mohamedoujdid.annotationplatform.auth.dto;

import lombok.Data;

@Data
public class LoginForm {
    private String email;
    private String password;
}