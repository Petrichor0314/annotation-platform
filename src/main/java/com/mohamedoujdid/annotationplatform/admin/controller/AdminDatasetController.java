package com.mohamedoujdid.annotationplatform.admin.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mohamedoujdid.annotationplatform.admin.dto.DatasetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.DatasetResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetResponse;
import com.mohamedoujdid.annotationplatform.admin.service.AdminDatasetService;
import com.mohamedoujdid.annotationplatform.labeling.service.LabelingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Controller
@RequestMapping("/admin/datasets")
@RequiredArgsConstructor
@Slf4j
public class AdminDatasetController {

    private final AdminDatasetService adminDatasetService;
    private final LabelingService labelingService; // For fetching label sets

    @GetMapping
    public String listDatasets(Model model) {
        List<DatasetResponse> datasets = adminDatasetService.getAllDatasets();
        model.addAttribute("datasets", datasets);
        log.info("Fetched {} datasets for listing.", datasets.size());
        return "admin/datasets";
    }

    @GetMapping("/create")
    public String showCreateDatasetForm(Model model) {
        if (!model.containsAttribute("datasetCreateRequest")) {
            model.addAttribute("datasetCreateRequest", new DatasetCreateRequest());
        }
        List<LabelSetResponse> labelSets = labelingService.getAllLabelSets();
        model.addAttribute("labelSets", labelSets);
        log.info("Showing create dataset form with {} label sets.", labelSets.size());
        return "admin/dataset-form";
    }

    @PostMapping("/create")
    public String createDataset(@Valid @ModelAttribute("datasetCreateRequest") DatasetCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                Model model) {

        if (request.getFile() == null || request.getFile().isEmpty()) {
            bindingResult.rejectValue("file", "error.file", "Please select a CSV or XLSX file to upload.");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while creating dataset: {}", bindingResult.getAllErrors());
            // Add labelSets again for the form redisplay
            List<LabelSetResponse> labelSets = labelingService.getAllLabelSets();
            model.addAttribute("labelSets", labelSets); // Add to model for direct render
            model.addAttribute("datasetCreateRequest", request); // Add request back to model
            // Don't use flash attributes if rendering the view directly
            // redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.datasetCreateRequest", bindingResult);
            // redirectAttributes.addFlashAttribute("datasetCreateRequest", request);
            return "admin/dataset-form"; // Render the form view directly to display errors
        }
        try {
            adminDatasetService.createDataset(request);
            redirectAttributes.addFlashAttribute("successMessage", "Dataset '" + request.getName() + "' creation initiated. Processing will continue in the background.");
            log.info("Dataset creation initiated for: {}", request.getName());
            return "redirect:/admin/datasets";
        } catch (IllegalArgumentException e) { // Catch specific exception for duplicate name or other validation
            log.warn("Error creating dataset {}: {}", request.getName(), e.getMessage());
            // Add labelSets again for the form redisplay
            List<LabelSetResponse> labelSets = labelingService.getAllLabelSets();
            model.addAttribute("labelSets", labelSets);
            model.addAttribute("datasetCreateRequest", request);
            // bindingResult.reject("globalError", e.getMessage()); // Or add as a global error
            model.addAttribute("errorMessage", e.getMessage()); // Pass specific error message
            return "admin/dataset-form"; // Render the form view directly
        } catch (IOException e) {
            log.error("IOException during dataset creation for {}: {}", request.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to process dataset file: " + e.getMessage());
        } catch (MaxUploadSizeExceededException e) {
            log.error("MaxUploadSizeExceededException for {}: {}", request.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "File size exceeds the maximum limit.");
        } catch (Exception e) {
            log.error("Unexpected error creating dataset {}: {}", request.getName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred during dataset creation.");
        }
        // If an error occurred and we haven't returned the form directly, redirect back to the form with the request and errors
        // This part might be less hit if specific exceptions render the form directly
        redirectAttributes.addFlashAttribute("datasetCreateRequest", request); // Keep form data
        return "redirect:/admin/datasets/create";
    }


    @GetMapping("/{id}")
    public String viewDataset(@PathVariable Long id,
                              @PageableDefault(size = 10, sort = "id") Pageable pageable,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            DatasetResponse datasetResponse = adminDatasetService.getDatasetResponseById(id, pageable);
            model.addAttribute("dataset", datasetResponse);
            log.info("Viewing dataset ID: {} with page {} and size {}", id, pageable.getPageNumber(), pageable.getPageSize());
            return "admin/dataset-detail";
        } catch (Exception e) { // Catching a broader exception for cases like ResourceNotFoundException
            log.error("Error fetching dataset with id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Could not retrieve dataset details: " + e.getMessage());
            return "redirect:/admin/datasets";
        }
    }
}
