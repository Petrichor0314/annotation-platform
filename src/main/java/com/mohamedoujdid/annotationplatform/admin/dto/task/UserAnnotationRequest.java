package com.mohamedoujdid.annotationplatform.admin.dto.task;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAnnotationRequest {

    // annotationTaskId will typically come from the path
    // annotatorId will come from the authenticated user context

    @NotNull(message = "TextPair ID cannot be null.")
    private Long textPairId;

    @NotNull(message = "ClassLabel ID cannot be null.")
    private Long classLabelId;
} 