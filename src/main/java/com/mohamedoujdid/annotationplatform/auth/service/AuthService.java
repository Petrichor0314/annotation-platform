package com.mohamedoujdid.annotationplatform.auth.service;

import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.service.Imp.UserServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserServiceImp userService;
    private final PasswordEncoder passwordEncoder;


}