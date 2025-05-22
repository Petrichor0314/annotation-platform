package com.mohamedoujdid.annotationplatform.auth.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.mohamedoujdid.annotationplatform.auth.dto.PasswordChangeForm;
import com.mohamedoujdid.annotationplatform.auth.service.PasswordService;
import com.mohamedoujdid.annotationplatform.config.CustomUserDetails;
import com.mohamedoujdid.annotationplatform.exception.InvalidCurrentPasswordException;
import com.mohamedoujdid.annotationplatform.user.model.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
            @Valid @ModelAttribute("form") PasswordChangeForm form,
            BindingResult result,
            Authentication authentication,
            Model model
    ) {
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "New password and confirmation do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("form", form); 
            System.out.println("MODEL: " + model.asMap());
            return "auth/password-change";
        }

        if (authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            User user = userDetails.getUser();

            try {
                passwordService.changePassword(
                        user.getEmail(),
                        form.getCurrentPassword(),
                        form.getNewPassword()
                );
            }catch (InvalidCurrentPasswordException e) {
                    result.rejectValue("currentPassword", "invalid", "Current password is incorrect");
                    model.addAttribute("form", form);
                System.out.println("MODEL: " + model.asMap());
                return "auth/password-change";
                }


            String roleName = user.getRole().getName();

            return switch (roleName) {
                case "ADMIN" -> "redirect:/admin/dashboard";
                case "ANNOTATOR" -> "redirect:/annotator/dashboard";
                default -> throw new IllegalStateException("Unexpected role: " + roleName);
            };

        }

        throw new IllegalStateException("Unexpected principal type: " +
                authentication.getPrincipal().getClass().getName());
    }



}