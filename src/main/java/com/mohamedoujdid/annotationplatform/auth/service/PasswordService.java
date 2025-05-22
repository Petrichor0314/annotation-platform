package com.mohamedoujdid.annotationplatform.auth.service;

public interface PasswordService {
    void changePassword(String email, String currentPassword, String newPassword);
}
