package com.mohamedoujdid.annotationplatform.admin.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatasetCreateRequest {

    @NotBlank(message = "Dataset name is required")
    private String name;

    private String description;

    @NotNull(message = "A file is required for the dataset.")
    private MultipartFile file;
} 