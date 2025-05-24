package com.mohamedoujdid.annotationplatform.annotator.dto;

import java.util.List;

import com.mohamedoujdid.annotationplatform.admin.dto.TextPairResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelResponse; // Assuming this is the correct DTO for a single text pair
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnnotationPageData {
    private AnnotationTaskResponse taskDetails;
    private TextPairResponse currentTextPair;
    private List<ClassLabelResponse> availableClassLabels;
    private int currentPageNumber; // 1-based for display
    private int totalTextPairPages;
    private Long previouslySelectedClassLabelId; // If this pair was already annotated by this user
    private boolean allPairsInTaskAnnotatedByCurrentUser;
    private Long nextTextPairId;
    private Long previousTextPairId;
    private int currentTextPairIndexInDataset; // 0-based index, if useful for direct navigation
    
    // Individual annotator progress
    private int annotatorProgressPercentage;
    private int annotatorCompletedPairs;
    private int totalPairsInTask;
} 