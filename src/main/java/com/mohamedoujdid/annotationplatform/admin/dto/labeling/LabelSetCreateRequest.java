package com.mohamedoujdid.annotationplatform.admin.dto.labeling;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LabelSetCreateRequest {
    @NotBlank(message = "Label Set name is required")
    private String name;
    private String description;
}
