package com.mohamedoujdid.annotationplatform.admin.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mohamedoujdid.annotationplatform.admin.dto.user.UpdateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.user.CreateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.service.AdminUserService;
import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import com.mohamedoujdid.annotationplatform.user.model.Role;
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
        List<Annotator> annotators = userService.getAllAnnotators();
        model.addAttribute("users", annotators);
        return "admin/user-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("form", new CreateUserRequest());
        model.addAttribute("roles", roleRepository.findAll());
        return "admin/create-user";
    }

    @PostMapping
    public String createUser(@ModelAttribute("form") @Valid CreateUserRequest form, 
                          org.springframework.validation.BindingResult bindingResult,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Return to form with validation errors
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/create-user";
        }
        
        try {
            userService.createUser(form);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/admin/users";
        } catch (Exception e) {
            // Handle any other exceptions
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            model.addAttribute("roles", roleRepository.findAll());
            return "admin/create-user";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            UpdateUserRequest user = userService.getUpdateUserForm(id);
            model.addAttribute("form", user);
            return "admin/edit-user";
        } catch (IllegalArgumentException e) {
            // This will be thrown if an admin tries to edit another admin
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute("form") @Valid UpdateUserRequest form, 
                           org.springframework.validation.BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Return to form with validation errors
            return "admin/edit-user";
        }
        
        try {
            // Get the current role of the user and keep it (should be ANNOTATOR)
            userService.updateUser(id, form, null);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            // This will be thrown if an admin tries to edit another admin
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            // This will be thrown if an admin tries to delete another admin
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
