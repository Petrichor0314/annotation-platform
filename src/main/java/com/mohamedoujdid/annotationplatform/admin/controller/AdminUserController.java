package com.mohamedoujdid.annotationplatform.admin.controller;

import com.mohamedoujdid.annotationplatform.admin.dto.CreateUserRequest;
import com.mohamedoujdid.annotationplatform.notification.EmailService;
import com.mohamedoujdid.annotationplatform.user.enums.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.service.Imp.UserServiceImp;
import com.mohamedoujdid.annotationplatform.utils.PasswordGenerator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserServiceImp userService;
    private final EmailService emailService;
    private final PasswordGenerator passwordGenerator;

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin/user-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("userRequest", new CreateUserRequest());
        return "admin/create-user";
    }

    @PostMapping
    public String createUser(@Valid CreateUserRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return "admin/create-user";
        }

        String tempPassword = passwordGenerator.generateSecurePassword(10);
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(tempPassword);
        user.setRole(Role.ANNOTATOR);
        user.setPasswordChangeRequired(true);

        userService.createUser(user);

        emailService.sendTemporaryCredentials(
                user.getEmail(),
                tempPassword
        );

        return "redirect:/admin/users";
    }
}