package com.mohamedoujdid.annotationplatform.annotation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mohamedoujdid.annotationplatform.annotation.dto.AnnotationDetailResponse;

public interface AnnotationService {
    
    /**
     * Get all annotations across all tasks with pagination
     * 
     * @param pageable Pagination information
     * @return A page of annotation details
     */
    Page<AnnotationDetailResponse> getAllAnnotations(Pageable pageable);
    
    /**
     * Get all annotations across all tasks without pagination (for export)
     * 
     * @return A list of all annotation details
     */
    List<AnnotationDetailResponse> getAllAnnotationsNoPage();
    
    /**
     * Get all annotations for a specific task with pagination
     * 
     * @param taskId The ID of the annotation task
     * @param pageable Pagination information
     * @return A page of annotation details
     */
    Page<AnnotationDetailResponse> getAnnotationsForTask(Long taskId, Pageable pageable);
    
    /**
     * Get all annotations for a specific task without pagination (for export)
     * 
     * @param taskId The ID of the annotation task
     * @return A list of all annotation details for the task
     */
    List<AnnotationDetailResponse> getAllAnnotationsForTaskNoPage(Long taskId);
    
    /**
     * Get all annotations by a specific annotator for a task
     * 
     * @param taskId The ID of the annotation task
     * @param annotatorId The ID of the annotator
     * @param pageable Pagination information
     * @return A page of annotation details
     */
    Page<AnnotationDetailResponse> getAnnotationsForTaskByAnnotator(Long taskId, Long annotatorId, Pageable pageable);
    
    /**
     * Get detailed information about a specific annotation
     * 
     * @param annotationId The ID of the annotation
     * @return Detailed annotation information
     */
    AnnotationDetailResponse getAnnotationDetail(Long annotationId);
    
    /**
     * Get a summary of annotation statistics for a task
     * 
     * @param taskId The ID of the annotation task
     * @return A map of statistics about the annotations
     */
    List<AnnotationDetailResponse> getAnnotationsForTextPair(Long taskId, Long textPairId);
}
