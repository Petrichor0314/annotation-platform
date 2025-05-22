package com.mohamedoujdid.annotationplatform.admin.dto.labeling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelSetBasicInfoResponse {
    private Long id;
    private String name;
} 