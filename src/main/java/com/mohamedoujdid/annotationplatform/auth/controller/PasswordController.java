package com.mohamedoujdid.annotationplatform.auth.controller;

import com.mohamedoujdid.annotationplatform.auth.dto.PasswordChangeForm;
import com.mohamedoujdid.annotationplatform.auth.service.AuthService;
import com.mohamedoujdid.annotationplatform.auth.service.PasswordService;
import com.mohamedoujdid.annotationplatform.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @GetMapping("/change")
    public String showChangeForm(@RequestParam(required = false) Boolean required, Model model) {
        if (Boolean.TRUE.equals(required)) {
            model.addAttribute("message", "Password change required");
        }
        model.addAttribute("form", new PasswordChangeForm());
        return "auth/password-change";
    }

    @PostMapping("/change")
    public String changePassword(
            @Valid PasswordChangeForm form,
            BindingResult result,
            Authentication authentication
    ) {
        if (result.hasErrors()) {
            return "auth/password-change";
        }

        User user = (User) authentication.getPrincipal();
        passwordService.changePassword(
                user.getEmail(),
                form.getCurrentPassword(),
                form.getNewPassword()
        );

        return "redirect:/user/dashboard";
    }
}