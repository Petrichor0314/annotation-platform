package com.mohamedoujdid.annotationplatform.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TextPairResponse {
    private Long id;
    private String text1;
    private String text2;
    private String metadata;
} 