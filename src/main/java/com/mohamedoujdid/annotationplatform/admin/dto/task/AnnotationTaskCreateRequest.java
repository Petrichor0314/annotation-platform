package com.mohamedoujdid.annotationplatform.admin.dto.task;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnotationTaskCreateRequest {

    @NotBlank(message = "Task name cannot be blank.")
    @Size(max = 255, message = "Task name must be less than 255 characters.")
    private String name;

    @Size(max = 10000, message = "Description must be less than 10000 characters.")
    private String description;

    @NotNull(message = "Dataset ID cannot be null.")
    private Long datasetId;

    @NotNull(message = "LabelSet ID cannot be null.")
    private Long labelSetId;

    @FutureOrPresent(message = "Deadline must be in the present or future.")
    private LocalDateTime deadline; // Optional

    @NotEmpty(message = "At least one annotator must be assigned.")
    private Set<Long> assignedAnnotatorIds;
}
