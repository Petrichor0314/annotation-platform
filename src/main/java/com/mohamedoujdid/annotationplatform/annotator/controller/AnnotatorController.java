package com.mohamedoujdid.annotationplatform.annotator.controller;

import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.UserAnnotationRequest;
import com.mohamedoujdid.annotationplatform.annotator.dto.AnnotationPageData;
import com.mohamedoujdid.annotationplatform.config.CustomUserDetails;
import com.mohamedoujdid.annotationplatform.task.service.AnnotationTaskService;
import com.mohamedoujdid.annotationplatform.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/annotator")
@RequiredArgsConstructor
@Slf4j
public class AnnotatorController {

    private final AnnotationTaskService annotationTaskService;

    @GetMapping("/tasks")
    public String listAssignedTasks(@AuthenticationPrincipal CustomUserDetails currentUserDetails,
                                    Model model,
                                    @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        User currentUser = currentUserDetails != null ? currentUserDetails.getUser() : null;
        if (currentUser == null) {
            log.warn("No authenticated user found when trying to list tasks.");
            return "redirect:/auth/login?error=true";
        }
        log.info("User {} requesting assigned tasks.", currentUser.getEmail());
        try {
            Page<AnnotationTaskResponse> tasksPage = annotationTaskService.getTasksForAnnotator(currentUser.getId(), pageable);
            model.addAttribute("tasksPage", tasksPage);
            model.addAttribute("pageTitle", "My Assigned Tasks");
            model.addAttribute("activePage", "tasks");
            log.debug("Found {} tasks for annotator ID: {}", tasksPage.getTotalElements(), currentUser.getId());
        } catch (Exception e) {
            log.error("Error fetching tasks for annotator ID: {}: {}", currentUser.getId(), e.getMessage(), e);
            model.addAttribute("errorMessage", "Could not retrieve your assigned tasks.");
            model.addAttribute("tasksPage", Page.empty(pageable)); 
        }
        return "annotator/tasks";
    }
    
    @GetMapping("/dashboard")
    public String annotatorDashboard(@AuthenticationPrincipal CustomUserDetails currentUserDetails, Model model) {
        User currentUser = currentUserDetails != null ? currentUserDetails.getUser() : null;
        if (currentUser == null) {
            log.warn("Annotator dashboard accessed with null currentUser, redirecting to login.");
            return "redirect:/auth/login";
        }
        log.info("User {} accessing annotator dashboard.", currentUser.getEmail());
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("pageTitle", "Annotator Dashboard");
        return "annotator/dashboard";
    }

    @GetMapping("/tasks/{taskId}/annotate")
    public String showAnnotationPage(@PathVariable Long taskId,
                                     @RequestParam(name = "textPairIndex", required = false) Integer requestedTextPairIndex,
                                     @AuthenticationPrincipal CustomUserDetails currentUserDetails,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = currentUserDetails != null ? currentUserDetails.getUser() : null;
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            log.info("Annotator {} requesting annotation page for task {}, index: {}",
                    currentUser.getEmail(), taskId, requestedTextPairIndex);

            AnnotationPageData pageData = annotationTaskService.getAnnotationPageData(
                    taskId, currentUser.getId(), requestedTextPairIndex);

            model.addAttribute("pageData", pageData);
            model.addAttribute("pageTitle", "Annotate Task: " + pageData.getTaskDetails().getName());
            
            // Create and populate the UserAnnotationRequest with the previously selected label
            UserAnnotationRequest userAnnotationRequest = new UserAnnotationRequest();
            userAnnotationRequest.setTextPairId(pageData.getCurrentTextPair() != null ? pageData.getCurrentTextPair().getId() : null);
            userAnnotationRequest.setClassLabelId(pageData.getPreviouslySelectedClassLabelId());
            model.addAttribute("userAnnotationRequest", userAnnotationRequest);

            // Add info messages
            if (pageData.getCurrentTextPair() == null) {
                model.addAttribute("infoMessage", "This task currently has no text pairs to annotate.");
            } else if (pageData.isAllPairsInTaskAnnotatedByCurrentUser()) {
                model.addAttribute("infoMessage",
                        "You have annotated all text pairs in this task! You can review your annotations or return to task list.");
            }

            return "annotator/annotation-page";
        } catch (SecurityException e) {
            log.warn("SecurityException for annotator {}: {}", currentUser.getEmail(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/annotator/tasks";
        } catch (Exception e) {
            log.error("Error loading annotation page for task {}: {}", taskId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading annotation page: " + e.getMessage());
            return "redirect:/annotator/tasks";
        }
    }

    @PostMapping("/tasks/{taskId}/annotate")
    public String submitAnnotation(@PathVariable Long taskId,
                                   @ModelAttribute UserAnnotationRequest userAnnotationRequest,
                                   @RequestParam String action,
                                   @RequestParam(name = "currentTextPairIndex") int currentTextPairIndex,
                                   @AuthenticationPrincipal CustomUserDetails currentUserDetails,
                                   RedirectAttributes redirectAttributes) {
        User currentUser = currentUserDetails != null ? currentUserDetails.getUser() : null;
        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        log.info("Annotator {} submitting for task {}, action: {}, currentIndex: {}",
                currentUser.getEmail(), taskId, action, currentTextPairIndex);

        try {
            switch (action) {
                case "validate_next":
                    // Validate and save annotation
                    if (userAnnotationRequest.getClassLabelId() == null) {
                        redirectAttributes.addFlashAttribute("validationErrorMessage",
                                "Please select a label before validating.");
                        return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + currentTextPairIndex;
                    }

                    // Save annotation
                    annotationTaskService.submitAnnotation(taskId, currentUser.getId(), userAnnotationRequest);
                    redirectAttributes.addFlashAttribute("successMessage", "Annotation saved!");

                    // Find the next unannotated text pair (or the next pair if we're not done)
                    Integer nextIndex = currentTextPairIndex + 1;

                    // Get annotation page data to see if we've reached the end
                    AnnotationPageData pageData = annotationTaskService.getAnnotationPageData(
                            taskId, currentUser.getId(), null);

                    if (pageData.isAllPairsInTaskAnnotatedByCurrentUser() || nextIndex >= pageData.getTotalTextPairPages()) {
                        // If all annotated or we're at the end, just stay at current index
                        return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + currentTextPairIndex;
                    }

                    return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + nextIndex;

                case "next_only":
                    // Just navigate to the next text pair without saving
                    int nextPageIndex = currentTextPairIndex + 1;
                    AnnotationPageData checkData = annotationTaskService.getAnnotationPageData(
                            taskId, currentUser.getId(), null);

                    if (nextPageIndex < checkData.getTotalTextPairPages()) {
                        return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + nextPageIndex;
                    }
                    break;

                case "previous_only":
                    // Just navigate to the previous text pair without saving
                    int prevPageIndex = currentTextPairIndex - 1;
                    if (prevPageIndex >= 0) {
                        return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + prevPageIndex;
                    }
                    break;

                default:
                    // Unknown action, stay on current page
                    break;
            }

            // Default: stay on current page
            return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + currentTextPairIndex;
        } catch (Exception e) {
            log.error("Error in annotation action for task {}: {}", taskId, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
            return "redirect:/annotator/tasks/" + taskId + "/annotate?textPairIndex=" + currentTextPairIndex;
        }
    }


} 