package com.mohamedoujdid.annotationplatform.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mohamedoujdid.annotationplatform.admin.dto.user.UpdateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.user.CreateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.service.AdminUserService;
import com.mohamedoujdid.annotationplatform.user.model.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.RoleRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService userService;
    private final RoleRepository roleRepository; 


    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateUserRequest());
        model.addAttribute("roles",roleRepository.findAll() );
        return "admin/create-user";
    }

    @PostMapping
    public String createUser(@ModelAttribute("form") @Valid CreateUserRequest form) {
        userService.createUser(form);
        return "redirect:/admin/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        UpdateUserRequest user = userService.getUpdateUserForm(id);
        model.addAttribute("form", user);
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/edit-user";
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("form") @Valid UpdateUserRequest form) {
        Role role = roleRepository.findById(form.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID"));
        userService.updateUser(id, form, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
