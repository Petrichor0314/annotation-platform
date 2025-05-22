package com.mohamedoujdid.annotationplatform.admin.dto.labeling;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassLabelCreateRequest {
    @NotBlank(message = "Class Label name is required")
    private String name;
    private String value; // Optional short code
    private String description;
    
}
