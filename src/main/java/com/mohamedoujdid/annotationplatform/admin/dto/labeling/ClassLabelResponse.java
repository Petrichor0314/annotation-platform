package com.mohamedoujdid.annotationplatform.admin.dto.labeling;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassLabelResponse {
    private Long id;
    private String name;
    private String value;
    private String description;
    private Long labelSetId; // Included for context, might not always be needed in UI
}
