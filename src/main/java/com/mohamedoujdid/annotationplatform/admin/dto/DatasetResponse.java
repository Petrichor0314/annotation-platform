package com.mohamedoujdid.annotationplatform.admin.dto;

import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Data
@Builder
public class DatasetResponse {
    private Long id;
    private String name;
    private String description;
    private DatasetImportStatus status;
    private String importErrorMessage;
    private String originalFileName;
    private Long fileSize;
    private String fileType;
    private Integer totalTextPairsInFile;
    private Integer processedTextPairsCount;
    private Integer failedTextPairsCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Page<TextPairResponse> textPairsPage;
} 