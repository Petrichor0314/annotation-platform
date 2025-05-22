package com.mohamedoujdid.annotationplatform.admin.dto.task;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserAnnotationResponse {
    private Long id;
    private Long annotationTaskId;
    private Long textPairId;
    private Long classLabelId;
    private String classLabelName; // For display
    private String classLabelValue; // For display
    private Long annotatorId;
    private String annotatorName; // For display (e.g., username or full name)
    private LocalDateTime createdAt;
} 