package com.mohamedoujdid.annotationplatform.admin.dto.dataset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatasetBasicInfoResponse {
    private Long id;
    private String name;
} 