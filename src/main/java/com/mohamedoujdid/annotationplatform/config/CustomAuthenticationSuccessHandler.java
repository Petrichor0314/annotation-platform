package com.mohamedoujdid.annotationplatform.config;

import com.mohamedoujdid.annotationplatform.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // Force password change if required
        if (user.isPasswordChangeRequired()) {
            redirectStrategy.sendRedirect(request, response, "/auth/password/change?required=true");
            return;
        }


        // Determine target URL based on role
        String targetUrl = determineTargetUrl(user);

        // Redirect logic
        if (response.isCommitted()) {
            /* logger.debug("Response already committed"); */
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    private String determineTargetUrl(User user) {
        String role = user.getRole().getName().toUpperCase();

        return switch (role) {
            case "ADMIN" -> "/admin/dashboard";
            case "ANNOTATOR" -> "/annotator/dashboard";
            default -> throw new IllegalStateException("Unknown role");
        };
    }
}
