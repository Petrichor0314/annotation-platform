package com.mohamedoujdid.annotationplatform.config;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage;

        if (exception instanceof BadCredentialsException) {
            errorMessage = "Invalid credentials";
        } else if (exception instanceof DisabledException) {
            errorMessage = "Account disabled";
        } else if (exception instanceof LockedException) {
            errorMessage = "Account locked";
        } else {
            errorMessage = "Authentication failed";
        }

        response.sendRedirect("/auth/login?error=" + URLEncoder.encode(errorMessage, StandardCharsets.UTF_8));
    }
}