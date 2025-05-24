package com.mohamedoujdid.annotationplatform.annotation.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationDetailResponse {
    private Long id;
    private Long taskId;
    private String taskName;
    
    // Text pair details
    private Long textPairId;
    private String text1;
    private String text2;
    private String textPairMetadata;
    
    // Label details
    private Long labelId;
    private String labelName;
    private String labelValue;
    private String selectedLabel; // The displayed label (name or value)
    
    // Annotator details
    private Long annotatorId;
    private String annotatorEmail;
    private String annotatorName;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
