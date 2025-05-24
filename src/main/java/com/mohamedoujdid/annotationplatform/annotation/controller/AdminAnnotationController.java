package com.mohamedoujdid.annotationplatform.annotation.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mohamedoujdid.annotationplatform.annotation.dto.AnnotationDetailResponse;
import com.mohamedoujdid.annotationplatform.annotation.service.AnnotationService;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;
import com.mohamedoujdid.annotationplatform.task.service.AnnotationTaskService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Writer;
import java.util.List;


import com.opencsv.CSVWriter;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/annotations")
@RequiredArgsConstructor
@Slf4j
public class AdminAnnotationController {

    private final AnnotationService annotationService;
    private final AnnotationTaskService annotationTaskService;

    /**
     * Show task selection interface with option to view all annotations
     */
    @GetMapping
    public String showTaskSelection(Model model) {
        try {
            log.info("Showing task selection for annotations");
            
            // Get all tasks for selection
            List<AnnotationTaskResponse> tasks = 
                annotationTaskService.getAllAnnotationTasks();
            
            model.addAttribute("tasks", tasks);
            model.addAttribute("pageTitle", "Select Task for Annotations");
            model.addAttribute("activePage", "annotations");
            
            return "admin/annotations/task-selection";
        } catch (Exception e) {
            log.error("Error fetching tasks for annotation selection: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error fetching tasks: " + e.getMessage());
            return "admin/annotations/task-selection";
        }
    }

    /**
     * List all annotations across all tasks with pagination
     */
    @GetMapping("/all")
    public String listAllAnnotations(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Model model) {
        
        try {
            log.info("Fetching all annotations");
            Page<AnnotationDetailResponse> annotationsPage = annotationService.getAllAnnotations(pageable);
            
            model.addAttribute("annotationsPage", annotationsPage);
            model.addAttribute("pageTitle", "All Annotations");
            model.addAttribute("activePage", "annotations");
            
            return "admin/annotations/list";
        } catch (Exception e) {
            log.error("Error fetching all annotations: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error fetching annotations: " + e.getMessage());
            return "admin/annotations/list";
        }
    }

    /**
     * List all annotations for a specific task with pagination
     */
    @GetMapping("/tasks/{taskId}")
    public String listAnnotationsForTask(
            @PathVariable Long taskId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Model model) {
        
        try {
            log.info("Fetching annotations for task ID: {}", taskId);
            Page<AnnotationDetailResponse> annotationsPage = annotationService.getAnnotationsForTask(taskId, pageable);
            
            model.addAttribute("annotationsPage", annotationsPage);
            model.addAttribute("taskId", taskId);
            model.addAttribute("taskDetails", annotationTaskService.getTaskResponseById(taskId));
            model.addAttribute("pageTitle", "Annotations for Task");
            model.addAttribute("activePage", "tasks");
            
            return "admin/annotations/list";
        } catch (Exception e) {
            log.error("Error fetching annotations for task ID: {}: {}", taskId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Error fetching annotations: " + e.getMessage());
            return "admin/annotations/list";
        }
    }

    /**
     * List annotations by a specific annotator for a task
     */
    @GetMapping("/tasks/{taskId}/annotators/{annotatorId}")
    public String listAnnotationsByAnnotator(
            @PathVariable Long taskId,
            @PathVariable Long annotatorId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable,
            Model model) {
        
        try {
            log.info("Fetching annotations for task ID: {} by annotator ID: {}", taskId, annotatorId);
            Page<AnnotationDetailResponse> annotationsPage = 
                    annotationService.getAnnotationsForTaskByAnnotator(taskId, annotatorId, pageable);
            
            model.addAttribute("annotationsPage", annotationsPage);
            model.addAttribute("taskId", taskId);
            model.addAttribute("annotatorId", annotatorId);
            model.addAttribute("taskDetails", annotationTaskService.getTaskResponseById(taskId));
            model.addAttribute("pageTitle", "Annotations by Annotator");
            model.addAttribute("activePage", "tasks");
            
            // If we have at least one annotation, we can get the annotator name from it
            if (!annotationsPage.isEmpty()) {
                AnnotationDetailResponse firstAnnotation = annotationsPage.getContent().get(0);
                model.addAttribute("annotatorName", firstAnnotation.getAnnotatorName());
                model.addAttribute("annotatorEmail", firstAnnotation.getAnnotatorEmail());
            }
            
            return "admin/annotations/list-by-annotator";
        } catch (Exception e) {
            log.error("Error fetching annotations for task ID: {} by annotator ID: {}: {}", 
                    taskId, annotatorId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Error fetching annotations: " + e.getMessage());
            return "admin/annotations/list-by-annotator";
        }
    }

    /**
     * View annotations for a specific text pair in a task
     */
    @GetMapping("/tasks/{taskId}/text-pairs/{textPairId}")
    public String viewAnnotationsForTextPair(
            @PathVariable Long taskId,
            @PathVariable Long textPairId,
            Model model) {
        
        try {
            log.info("Fetching annotations for task ID: {} and text pair ID: {}", taskId, textPairId);
            List<AnnotationDetailResponse> annotations = 
                    annotationService.getAnnotationsForTextPair(taskId, textPairId);
            
            model.addAttribute("annotations", annotations);
            model.addAttribute("taskId", taskId);
            model.addAttribute("textPairId", textPairId);
            model.addAttribute("taskDetails", annotationTaskService.getTaskResponseById(taskId));
            model.addAttribute("pageTitle", "Annotations for Text Pair");
            model.addAttribute("activePage", "tasks");
            
            // If we have at least one annotation, we can get the text pair details from it
            if (!annotations.isEmpty()) {
                AnnotationDetailResponse firstAnnotation = annotations.get(0);
                model.addAttribute("text1", firstAnnotation.getText1());
                model.addAttribute("text2", firstAnnotation.getText2());
                model.addAttribute("textPairMetadata", firstAnnotation.getTextPairMetadata());
            }
            
            return "admin/annotations/text-pair-annotations";
        } catch (Exception e) {
            log.error("Error fetching annotations for task ID: {} and text pair ID: {}: {}", 
                    taskId, textPairId, e.getMessage(), e);
            model.addAttribute("errorMessage", "Error fetching annotations: " + e.getMessage());
            return "admin/annotations/text-pair-annotations";
        }
    }

    /**
     * View detailed information about a specific annotation
     */
    @GetMapping("/{annotationId}")
    public String viewAnnotationDetail(
            @PathVariable Long annotationId,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        try {
            log.info("Fetching annotation detail for ID: {}", annotationId);
            AnnotationDetailResponse annotation = annotationService.getAnnotationDetail(annotationId);
            
            model.addAttribute("annotation", annotation);
            model.addAttribute("pageTitle", "Annotation Detail");
            model.addAttribute("activePage", "tasks");
            
            return "admin/annotations/detail";
        } catch (Exception e) {
            log.error("Error fetching annotation detail for ID: {}: {}", annotationId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error fetching annotation: " + e.getMessage());
            return "redirect:/admin/tasks";
        }
    }

    /**
     * Export annotations for a task as CSV
     */
    @GetMapping("/tasks/{taskId}/export")
    public void exportAnnotationsForTask(
            @PathVariable Long taskId,
            jakarta.servlet.http.HttpServletResponse response) throws IOException {
        
        try {
            log.info("Exporting annotations for task ID: {}", taskId);
            
            // Set response headers
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=annotations-task-" + taskId + ".csv");
            
            // Get all annotations for the task (no pagination for export)
            List<AnnotationDetailResponse> annotations = annotationService.getAllAnnotationsForTaskNoPage(taskId);
            
            // Write to CSV
            try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
                // Write header
                csvWriter.writeNext(new String[] {
                    "Text 1", "Text 2", "Class Label", "Annotator", "Date"
                });
                
                // Write data rows
                for (AnnotationDetailResponse annotation : annotations) {
                    csvWriter.writeNext(new String[] {
                        annotation.getText1(),
                        annotation.getText2(),
                        annotation.getSelectedLabel(),
                        annotation.getAnnotatorName(),
                        annotation.getCreatedAt().toString()
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error exporting annotations for task ID: {}: {}", taskId, e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting annotations: " + e.getMessage());
        }
    }
    
    /**
     * Export all annotations as CSV
     */
    @GetMapping("/export")
    public void exportAllAnnotations(HttpServletResponse response) throws IOException {
        
        try {
            log.info("Exporting all annotations");
            
            // Set response headers
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=all-annotations.csv");
            
            // Get all annotations (no pagination for export)
            List<AnnotationDetailResponse> annotations = annotationService.getAllAnnotationsNoPage();
            
            // Write to CSV
            try (CSVWriter csvWriter = new CSVWriter(response.getWriter())) {
                // Write header
                csvWriter.writeNext(new String[] {
                    "Task", "Text 1", "Text 2", "Class Label", "Annotator", "Date"
                });
                
                // Write data rows
                for (AnnotationDetailResponse annotation : annotations) {
                    csvWriter.writeNext(new String[] {
                        annotation.getTaskName(),
                        annotation.getText1(),
                        annotation.getText2(),
                        annotation.getSelectedLabel(),
                        annotation.getAnnotatorName(),
                        annotation.getCreatedAt().toString()
                    });
                }
            }
        } catch (Exception e) {
            log.error("Error exporting all annotations: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error exporting annotations: " + e.getMessage());
        }
    }
}
