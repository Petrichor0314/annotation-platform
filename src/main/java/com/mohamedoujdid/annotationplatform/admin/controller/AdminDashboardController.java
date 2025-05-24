package com.mohamedoujdid.annotationplatform.admin.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mohamedoujdid.annotationplatform.annotation.repository.AnnotationRepository;
import com.mohamedoujdid.annotationplatform.dataset.repository.DatasetRepository;
import com.mohamedoujdid.annotationplatform.task.repository.AnnotationTaskRepository;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final UserRepository userRepository;
    private final AnnotationRepository annotationRepository;
    private final AnnotationTaskRepository taskRepository;
    private final DatasetRepository datasetRepository;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // Get counts for dashboard stats
        long totalUsers = userRepository.countByDeletedFalseAndRole_name("ADMIN");
        long totalAnnotations = annotationRepository.count();   
        long pendingTasks = taskRepository.countByCompletionPercentageLessThan(100);
        long totalDatasets = datasetRepository.count();
        
        // Add stats to model
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAnnotations", totalAnnotations);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("totalDatasets", totalDatasets);
        
        // Get recent activity (last 5 users)
        List<User> recentUsers = userRepository.findTop5ByDeletedFalseOrderByIdDesc();
        model.addAttribute("recentUsers", recentUsers);
        
        // Add current date/time for dashboard
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        model.addAttribute("currentDate", now.format(formatter));
        
        // Add task completion data for chart
        Map<String, Long> taskCompletionData = new HashMap<>();
        taskCompletionData.put("Completed", taskRepository.countByCompletionPercentage(100));
        taskCompletionData.put("In Progress", taskRepository.countByCompletionPercentageBetween(1, 99));
        taskCompletionData.put("Not Started", taskRepository.countByCompletionPercentage(0));
        model.addAttribute("taskCompletionData", taskCompletionData);
        
        return "admin/dashboard";
    }
}
