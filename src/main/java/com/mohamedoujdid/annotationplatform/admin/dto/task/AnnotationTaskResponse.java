package com.mohamedoujdid.annotationplatform.admin.dto.task;

import com.mohamedoujdid.annotationplatform.admin.dto.user.UserSummaryResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class AnnotationTaskResponse {
    private Long id;
    private String name;
    private String description;

    private Long datasetId;
    private String datasetName; // For display purposes

    private Long labelSetId;
    private String labelSetName; // For display purposes

    private Integer completionPercentage;
    private LocalDateTime deadline;
    private Set<UserSummaryResponse> assignedAnnotators;
    private Integer totalTextPairsInTask; // Will be dataset.getTextPairs().size()
    private Integer completedTextPairsInTask; // Count of text pairs where all assigned annotators have provided a label

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
