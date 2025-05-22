package com.mohamedoujdid.annotationplatform.admin.controller;

import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetResponse;
import com.mohamedoujdid.annotationplatform.labeling.service.LabelingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mohamedoujdid.annotationplatform.labeling.service.LabelingService;

@Controller
@RequestMapping("/admin/labeling")
@RequiredArgsConstructor
public class AdminLabelingController {

    private final LabelingService labelingService;

    // == LabelSet Endpoints ==
    @GetMapping("/sets")
    public String listLabelSets(Model model) {
        model.addAttribute("labelSets", labelingService.getAllLabelSets());
        return "admin/labelset-list";
    }

    @GetMapping("/sets/create")
    public String showCreateLabelSetForm(Model model) {
        if (!model.containsAttribute("labelSetCreateRequest")) {
            model.addAttribute("labelSetCreateRequest", new LabelSetCreateRequest());
        }
        return "admin/labelset-form";
    }

    @PostMapping("/sets/create")
    public String createLabelSet(@Valid @ModelAttribute LabelSetCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.labelSetCreateRequest", bindingResult);
            redirectAttributes.addFlashAttribute("labelSetCreateRequest", request);
            return "redirect:/admin/labeling/sets/create"; // Redirect back to form with errors
        }
        try {
            labelingService.createLabelSet(request);
            redirectAttributes.addFlashAttribute("successMessage", "Label Set created successfully!");
            return "redirect:/admin/labeling/sets";
        } catch (Exception e) {
            // Consider custom exception for duplicate name, etc.
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating Label Set: " + e.getMessage());
            redirectAttributes.addFlashAttribute("labelSetCreateRequest", request);
            return "redirect:/admin/labeling/sets/create";
        }
    }

    @GetMapping("/sets/{id}")
    public String viewLabelSet(@PathVariable Long id, Model model) {
        LabelSetResponse labelSet = labelingService.getLabelSetResponseById(id);
        model.addAttribute("labelSet", labelSet);
        if (!model.containsAttribute("classLabelCreateRequest")) {
            model.addAttribute("classLabelCreateRequest", new ClassLabelCreateRequest());
        }
        return "admin/labelset-detail";
    }

    // == ClassLabel Endpoints (within a LabelSet context) ==
    @PostMapping("/sets/{labelSetId}/labels/create")
    public String createClassLabel(@PathVariable Long labelSetId,
                                   @Valid @ModelAttribute("classLabelCreateRequest") ClassLabelCreateRequest request,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            LabelSetResponse labelSet = labelingService.getLabelSetResponseById(labelSetId);
            model.addAttribute("labelSet", labelSet);
            model.addAttribute("errorMessageCL", "Validation failed. Please check field errors.");
            return "admin/labelset-detail";
        }
        try {
            labelingService.createClassLabel(labelSetId, request);
            redirectAttributes.addFlashAttribute("successMessageCL", "Class Label added successfully!");
        } catch (Exception e) {
            System.err.println("Error creating class label: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessageCL", "Error adding Class Label: " + e.getMessage());
            redirectAttributes.addFlashAttribute("classLabelCreateRequest", request);
        }
        return "redirect:/admin/labeling/sets/" + labelSetId;
    }
}
