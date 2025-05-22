package com.mohamedoujdid.annotationplatform.admin.controller;

import com.mohamedoujdid.annotationplatform.admin.dto.dataset.DatasetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.user.UserSummaryResponse;
import com.mohamedoujdid.annotationplatform.task.service.AnnotationTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
@Slf4j
public class AnnotationTaskController {

    private final AnnotationTaskService annotationTaskService;
    // private final AdminDatasetService adminDatasetService; // For fetching datasets
    // private final LabelingService labelingService; // For fetching label sets
    // private final UserService userService; // For fetching users (annotators)

    private void addCommonFormAttributes(Model model) {
        List<DatasetBasicInfoResponse> datasets = annotationTaskService.getAllDatasetsForSelection();
        List<LabelSetBasicInfoResponse> labelSets = annotationTaskService.getAllLabelSetsForSelection();
        List<UserSummaryResponse> annotators = annotationTaskService.getAllAnnotatorsForSelection();
        model.addAttribute("datasets", datasets);
        model.addAttribute("labelSets", labelSets);
        model.addAttribute("annotators", annotators);
    }

    @GetMapping
    public String listAnnotationTasks(Model model) {
        List<AnnotationTaskResponse> tasks = annotationTaskService.getAllAnnotationTasks();
        model.addAttribute("tasks", tasks);
        log.info("Fetched {} annotation tasks for listing.", tasks.size());
        return "admin/tasks";
    }

    @GetMapping("/create")
    public String showCreateTaskForm(Model model) {
        if (!model.containsAttribute("taskRequest")) {
            model.addAttribute("taskRequest", new AnnotationTaskCreateRequest());
        }
        addCommonFormAttributes(model);
        return "admin/task-form";
    }

    @PostMapping("/create")
    public String createAnnotationTask(@Valid @ModelAttribute("taskRequest") AnnotationTaskCreateRequest request,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while creating annotation task: {}", bindingResult.getAllErrors());
            addCommonFormAttributes(model); // For direct render if not redirecting
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.taskRequest", bindingResult);
            redirectAttributes.addFlashAttribute("taskRequest", request);
            return "redirect:/admin/tasks/create";
        }
        try {
            annotationTaskService.createTask(request);
            redirectAttributes.addFlashAttribute("successMessage", "Annotation Task '" + request.getName() + "' created successfully.");
            log.info("Annotation Task creation successful for: {}", request.getName());
            return "redirect:/admin/tasks";
        } catch (Exception e) {
            log.error("Error creating annotation task {}: {}", request.getName(), e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create task: " + e.getMessage());
            redirectAttributes.addFlashAttribute("taskRequest", request); // Keep form data
            return "redirect:/admin/tasks/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditTaskForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            if (!model.containsAttribute("taskRequest")) {
                 AnnotationTaskCreateRequest taskRequest = annotationTaskService.getTaskUpdateRequestById(id);
                 model.addAttribute("taskRequest", taskRequest);
            }
            model.addAttribute("editMode", true);
            model.addAttribute("currentTaskId", id);
            addCommonFormAttributes(model);
            return "admin/task-form";
        } catch (Exception e) {
            log.error("Error fetching task for edit with id {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Could not retrieve task for editing: " + e.getMessage());
            return "redirect:/admin/tasks";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateAnnotationTask(@PathVariable Long id,
                                     @Valid @ModelAttribute("taskRequest") AnnotationTaskCreateRequest request,
                                     BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while updating annotation task {}: {}", id, bindingResult.getAllErrors());
            model.addAttribute("editMode", true);
            addCommonFormAttributes(model); // For direct render if not redirecting
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.taskRequest", bindingResult);
            redirectAttributes.addFlashAttribute("taskRequest", request);
            return "redirect:/admin/tasks/" + id + "/edit";
        }
        try {
            annotationTaskService.updateTask(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Annotation Task '" + request.getName() + "' updated successfully.");
            log.info("Annotation Task update successful for ID: {}", id);
            return "redirect:/admin/tasks";
        } catch (Exception e) {
            log.error("Error updating annotation task ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update task: " + e.getMessage());
            redirectAttributes.addFlashAttribute("taskRequest", request);
            return "redirect:/admin/tasks/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteAnnotationTask(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            annotationTaskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("successMessage", "Annotation Task deleted successfully.");
            log.info("Annotation Task deleted successfully for ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting annotation task ID {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete task: " + e.getMessage());
        }
        return "redirect:/admin/tasks";
    }
} 