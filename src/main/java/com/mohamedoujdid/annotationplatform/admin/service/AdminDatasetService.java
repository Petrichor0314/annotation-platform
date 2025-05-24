package com.mohamedoujdid.annotationplatform.admin.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.mohamedoujdid.annotationplatform.admin.dto.DatasetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.DatasetResponse;
import com.mohamedoujdid.annotationplatform.dataset.model.Dataset;
import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus;
import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;

public interface AdminDatasetService {

    DatasetResponse createDataset(DatasetCreateRequest request) throws IOException;

    List<DatasetResponse> getAllDatasets();

    DatasetResponse getDatasetResponseById(Long id, Pageable pageable);

    Dataset getDatasetById(Long id);

    // New methods for update and delete
    DatasetResponse updateDataset(Long id, DatasetCreateRequest request) throws IOException;

    void deleteDataset(Long id);

    // Async processing for file import
    void processAndSaveTextPairsAsync(String tempFilePathStr, String originalFilename, long fileSize, String contentType, Long datasetId);

    // Method for saving a batch of text pairs and returning counts
    // Consider moving ProcessedBatchResult to a shared DTO package if used elsewhere or make it a top-level record in the impl
    com.mohamedoujdid.annotationplatform.admin.service.impl.AdminDatasetServiceImpl.ProcessedBatchResult saveTextPairBatch(List<TextPair> batch, Long datasetId);

    // Method for updating dataset import status
    void updateDatasetImportStatus(Long datasetId, DatasetImportStatus importStatus, String importErrorMessage, Integer totalTextPairsInFile, Integer processedTextPairsCount, Integer failedTextPairsCount);
}