package com.mohamedoujdid.annotationplatform.admin.dto.labeling;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LabelSetResponse {
    private Long id;
    private String name;
    private String description;
    private List<ClassLabelResponse> classLabels; // List of associated class labels
    private int classLabelCount; 
}
