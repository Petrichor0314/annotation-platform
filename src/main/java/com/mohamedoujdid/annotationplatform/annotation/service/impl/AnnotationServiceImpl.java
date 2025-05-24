package com.mohamedoujdid.annotationplatform.annotation.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mohamedoujdid.annotationplatform.annotation.dto.AnnotationDetailResponse;
import com.mohamedoujdid.annotationplatform.annotation.service.AnnotationService;
import com.mohamedoujdid.annotationplatform.task.model.UserAnnotation;
import com.mohamedoujdid.annotationplatform.task.repository.UserAnnotationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnotationServiceImpl implements AnnotationService {
    
    private final UserAnnotationRepository userAnnotationRepository;
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationDetailResponse> getAllAnnotations(Pageable pageable) {
        log.info("Fetching all annotations with pagination");
        Page<UserAnnotation> annotations = userAnnotationRepository.findAll(pageable);
        return annotations.map(this::mapToAnnotationDetailResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnotationDetailResponse> getAllAnnotationsNoPage() {
        log.info("Fetching all annotations without pagination");
        List<UserAnnotation> annotations = userAnnotationRepository.findAll();
        return annotations.stream()
                .map(this::mapToAnnotationDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationDetailResponse> getAnnotationsForTask(Long taskId, Pageable pageable) {
        log.info("Fetching annotations for task ID: {} with pagination", taskId);
        Page<UserAnnotation> annotations = userAnnotationRepository.findByAnnotationTaskId(taskId, pageable);
        return annotations.map(this::mapToAnnotationDetailResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnotationDetailResponse> getAllAnnotationsForTaskNoPage(Long taskId) {
        log.info("Fetching all annotations for task ID: {} without pagination", taskId);
        List<UserAnnotation> annotations = userAnnotationRepository.findByAnnotationTaskId(taskId);
        return annotations.stream()
                .map(this::mapToAnnotationDetailResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationDetailResponse> getAnnotationsForTaskByAnnotator(Long taskId, Long annotatorId, Pageable pageable) {
        log.info("Fetching annotations for task ID: {} by annotator ID: {} with pagination", taskId, annotatorId);
        Page<UserAnnotation> annotations = userAnnotationRepository.findByAnnotationTaskIdAndAnnotatorId(taskId, annotatorId, pageable);
        return annotations.map(this::mapToAnnotationDetailResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public AnnotationDetailResponse getAnnotationDetail(Long annotationId) {
        log.info("Fetching annotation detail for ID: {}", annotationId);
        UserAnnotation annotation = userAnnotationRepository.findById(annotationId)
                .orElseThrow(() -> new RuntimeException("Annotation not found with ID: " + annotationId));
        return mapToAnnotationDetailResponse(annotation);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<AnnotationDetailResponse> getAnnotationsForTextPair(Long taskId, Long textPairId) {
        log.info("Fetching annotations for task ID: {} and text pair ID: {}", taskId, textPairId);
        List<UserAnnotation> annotations = userAnnotationRepository.findByAnnotationTaskIdAndTextPairId(taskId, textPairId);
        return annotations.stream()
                .map(this::mapToAnnotationDetailResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps a UserAnnotation entity to an AnnotationDetailResponse DTO
     */
    private AnnotationDetailResponse mapToAnnotationDetailResponse(UserAnnotation annotation) {
        String annotatorName = annotation.getAnnotator().getFirstName() + " " + annotation.getAnnotator().getLastName();
        
        // Determine the selected label (use value if available, otherwise use name)
        String selectedLabel = annotation.getClassLabel().getValue() != null && !annotation.getClassLabel().getValue().isEmpty() 
                ? annotation.getClassLabel().getValue() 
                : annotation.getClassLabel().getName();
        
        return AnnotationDetailResponse.builder()
                .id(annotation.getId())
                .taskId(annotation.getAnnotationTask().getId())
                .taskName(annotation.getAnnotationTask().getName())
                
                // Text pair details
                .textPairId(annotation.getTextPair().getId())
                .text1(annotation.getTextPair().getText1())
                .text2(annotation.getTextPair().getText2())
                .textPairMetadata(annotation.getTextPair().getMetadata())
                
                // Label details
                .labelId(annotation.getClassLabel().getId())
                .labelName(annotation.getClassLabel().getName())
                .labelValue(annotation.getClassLabel().getValue())
                .selectedLabel(selectedLabel)
                
                // Annotator details
                .annotatorId(annotation.getAnnotator().getId())
                .annotatorEmail(annotation.getAnnotator().getEmail())
                .annotatorName(annotatorName)
                
                // Timestamps
                .createdAt(annotation.getCreatedAt())
                .updatedAt(annotation.getUpdatedAt())
                .build();
    }
}
