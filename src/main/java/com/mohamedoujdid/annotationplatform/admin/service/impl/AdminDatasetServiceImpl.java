package com.mohamedoujdid.annotationplatform.admin.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.mohamedoujdid.annotationplatform.admin.dto.DatasetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.DatasetResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.TextPairResponse;
import com.mohamedoujdid.annotationplatform.admin.service.AdminDatasetService;
import com.mohamedoujdid.annotationplatform.dataset.model.Dataset;
import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus;
import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;
import com.mohamedoujdid.annotationplatform.dataset.repository.DatasetRepository;
import com.mohamedoujdid.annotationplatform.dataset.repository.TextPairRepository;
import com.mohamedoujdid.annotationplatform.exception.ResourceNotFoundException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AdminDatasetServiceImpl implements AdminDatasetService {

    public static record ProcessedBatchResult(int success, int errors) {}

    private final DatasetRepository datasetRepository;
    private final TextPairRepository textPairRepository;
    private static final int PARSER_BATCH_SIZE = 1000;

    @Value("${app.upload.temp-dir:#{systemProperties['java.io.tmpdir']}}")
    private String uploadTempDir;

    private final AdminDatasetService self;

    @Autowired
    public AdminDatasetServiceImpl(DatasetRepository datasetRepository,
                                   TextPairRepository textPairRepository,
                                   @Lazy AdminDatasetService self) {
        this.datasetRepository = datasetRepository;
        this.textPairRepository = textPairRepository;
        this.self = self;
    }

    @Override
    @Transactional
    public DatasetResponse createDataset(DatasetCreateRequest request) throws IOException {
        // Check for duplicate dataset name
        if (datasetRepository.existsByNameIgnoreCase(request.getName())) {
            log.warn("Attempt to create dataset with duplicate name: {}", request.getName());
            // Throw an exception that the controller can catch and handle
            throw new IllegalArgumentException("A dataset with the name '" + request.getName() + "' already exists.");
        }

        MultipartFile file = request.getFile();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown_file.tmp";

        Dataset dataset = Dataset.builder()
                .name(request.getName())
                .description(request.getDescription())
                .importStatus(DatasetImportStatus.PENDING) // Initial status before file handling
                .originalFilename(originalFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .processedTextPairsCount(0)
                .failedTextPairsCount(0)
                .build();
        Dataset savedDataset = datasetRepository.save(dataset);
        log.info("Initial dataset record saved with ID: {} and ImportStatus: {}", savedDataset.getId(), savedDataset.getImportStatus());

        final Long datasetId = savedDataset.getId(); // Use final variable for lambda

        if (file != null && !file.isEmpty()) {
            Path tempDir = Paths.get(uploadTempDir);
            if (!Files.exists(tempDir)) {
                 Files.createDirectories(tempDir);
            }
            String storedFilename = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path tempFilePath = tempDir.resolve(storedFilename);
            final String tempFilePathStr = tempFilePath.toString(); // final for lambda
            final String finalOriginalFilename = originalFilename; // final for lambda
            final long fileSize = file.getSize(); // final for lambda
            final String contentType = file.getContentType(); // final for lambda

            try {
                Files.copy(file.getInputStream(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);
                // Update dataset with stored filename within the same transaction
                Dataset datasetToUpdate = datasetRepository.findById(datasetId).orElseThrow(() -> 
                    new EntityNotFoundException("Dataset disappeared before file path update: " + datasetId));
                datasetToUpdate.setStoredFilename(storedFilename);
                datasetToUpdate.setImportStatus(DatasetImportStatus.PENDING_UPLOAD_COMPLETION); // Intermediate status
                datasetRepository.save(datasetToUpdate);
                log.info("File for dataset ID {} saved temporarily to: {} and dataset record updated", datasetId, tempFilePath);

                // Schedule async processing after transaction commit
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        log.info("Transaction committed for dataset ID {}. Initiating async processing.", datasetId);
                        // First, update status to UPLOADING (this is a new transaction via self call)
                        self.updateDatasetImportStatus(datasetId, DatasetImportStatus.UPLOADING, null, null, 0, 0);
                        // Then, start the actual async processing
                        self.processAndSaveTextPairsAsync(tempFilePathStr, finalOriginalFilename, fileSize, contentType, datasetId);
                    }
                });
            } catch (IOException e) {
                log.error("Failed to save uploaded file for dataset ID {}: {}", datasetId, e.getMessage());
                // Update status to FAILED_UPLOAD directly as this is an error within the current transaction scope
                // This needs to be callable without self to avoid transactional proxy issues if called during rollback
                updateDatasetStatusDirectly(datasetId, DatasetImportStatus.FAILED_UPLOAD, "Failed to save uploaded file: " + e.getMessage());
                // No need to throw, return a response with error status
            }
        } else {
            log.warn("No file provided for dataset ID: {}. Marking as FAILED_VALIDATION.", datasetId);
            // Update status directly
            updateDatasetStatusDirectly(datasetId, DatasetImportStatus.FAILED_VALIDATION, "No file provided.");
        }
        // Fetch the latest state of the dataset for the response
        Dataset finalDataset = datasetRepository.findById(datasetId).orElse(savedDataset); 
        return mapToDatasetResponse(finalDataset, null);
    }

    // Helper method to update status without going through self-proxy if in error/rollback path
    private void updateDatasetStatusDirectly(Long datasetId, DatasetImportStatus status, String errorMessage) {
        try {
            Dataset dataset = datasetRepository.findById(datasetId).orElse(null);
            if (dataset != null) {
                dataset.setImportStatus(status);
                dataset.setImportErrorMessage(errorMessage);
                dataset.setUpdatedAt(LocalDateTime.now());
                datasetRepository.save(dataset);
                log.info("Dataset status updated directly for ID {} to {}", datasetId, status);
            } else {
                log.warn("Dataset with ID {} not found for direct status update.", datasetId);
            }
        } catch (Exception e) {
            log.error("Error during direct status update for dataset ID {}: {}", datasetId, e.getMessage(), e);
        }
    }

    @Override
    @Async("taskExecutor")
    // Corrected 5-argument signature
    public void processAndSaveTextPairsAsync(String tempFilePathStr, String originalFilename, long fileSize, String contentType, Long datasetId) {
        Path tempFilePath = Paths.get(tempFilePathStr);
        log.info("Starting asynchronous processing for dataset ID: {}, file: {}", datasetId, originalFilename);
        // Initial status update: total, processed, failed are 0 or null
        self.updateDatasetImportStatus(datasetId, DatasetImportStatus.VALIDATING, null, null, 0, 0);

        if (fileSize == 0) {
            self.updateDatasetImportStatus(datasetId, DatasetImportStatus.FAILED_VALIDATION, "File is empty.", 0, 0, 0);
            deleteTempFile(tempFilePath);
            return;
        }
        boolean isCsv = originalFilename != null && originalFilename.toLowerCase().endsWith(".csv");
        boolean isXlsx = originalFilename != null && originalFilename.toLowerCase().endsWith(".xlsx");

        if (!isCsv && !isXlsx) {
            self.updateDatasetImportStatus(datasetId, DatasetImportStatus.FAILED_VALIDATION, "Unsupported file type: " + originalFilename + ". Only CSV and XLSX are supported.", 0, 0, 0);
            deleteTempFile(tempFilePath);
            return;
        }

        self.updateDatasetImportStatus(datasetId, DatasetImportStatus.IMPORTING, null, null, 0, 0);

        AtomicInteger totalParsedFromFile = new AtomicInteger(0);
        AtomicInteger successfullySavedCount = new AtomicInteger(0);
        AtomicInteger failedToSaveCount = new AtomicInteger(0);

        Consumer<List<TextPair>> batchProcessor = batch -> {
            ProcessedBatchResult result = saveTextPairBatch(batch, datasetId);
            successfullySavedCount.addAndGet(result.success());
            failedToSaveCount.addAndGet(result.errors());
            // Update status incrementally after each batch
            // We fetch the current total parsed so far for the update.
            // This might be too frequent; consider updating less often for very large files.
             self.updateDatasetImportStatus(datasetId, DatasetImportStatus.IMPORTING, null, totalParsedFromFile.get(), successfullySavedCount.get(), failedToSaveCount.get());
        };

        try (InputStream inputStream = new FileInputStream(tempFilePath.toFile())) {
            int parsedThisRun;
            if (isCsv) {
                parsedThisRun = parseCsvAndProcessInBatches(inputStream, batchProcessor, datasetId, totalParsedFromFile);
            } else { // isXlsx
                parsedThisRun = parseXlsxAndProcessInBatches(inputStream, batchProcessor, datasetId, totalParsedFromFile);
            }
            // totalParsedFromFile is updated by side-effect in parsing methods

            // Final status update after all processing
            DatasetImportStatus finalStatus = failedToSaveCount.get() > 0 ? DatasetImportStatus.COMPLETED_WITH_ERRORS : DatasetImportStatus.COMPLETED;
            if (successfullySavedCount.get() == 0 && totalParsedFromFile.get() > 0) { // All parsed items failed to save
                 finalStatus = DatasetImportStatus.FAILED_IMPORT;
            } else if (totalParsedFromFile.get() == 0 && fileSize > 0) { // File was not empty but no valid text pairs found
                 finalStatus = DatasetImportStatus.FAILED_IMPORT; // Or COMPLETED_WITH_ERRORS if that makes more sense
                 self.updateDatasetImportStatus(datasetId, finalStatus, "No valid text pairs found in the file.", 0, 0, 0);
                 log.info("Asynchronous processing for dataset ID: {} complete. No valid text pairs found.", datasetId);
                 return; // Exit after setting status
            }

            self.updateDatasetImportStatus(datasetId, finalStatus, null, totalParsedFromFile.get(), successfullySavedCount.get(), failedToSaveCount.get());
            log.info("Finished asynchronous processing for dataset ID: {}. Total items in file: {}, Successfully saved: {}, Failed to save: {}",
                    datasetId, totalParsedFromFile.get(), successfullySavedCount.get(), failedToSaveCount.get());

        } catch (IOException e) {
            log.error("Error during file parsing/streaming for dataset ID {}: {}", datasetId, e.getMessage(), e);
            self.updateDatasetImportStatus(datasetId, DatasetImportStatus.FAILED_IMPORT, "Import Error: " + e.getMessage(), totalParsedFromFile.get(), successfullySavedCount.get(), failedToSaveCount.get());
        } catch (Exception e) {
            log.error("Unexpected error during asynchronous processing for dataset ID {}: {}", datasetId, e.getMessage(), e);
            self.updateDatasetImportStatus(datasetId, DatasetImportStatus.FAILED_IMPORT, "Unexpected import error: " + e.getMessage(), totalParsedFromFile.get(), successfullySavedCount.get(), failedToSaveCount.get());
        } finally {
            deleteTempFile(tempFilePath);
        }
    }

    private void deleteTempFile(Path tempFilePath) {
        try {
            Files.deleteIfExists(tempFilePath);
            log.info("Temporary file deleted: {}", tempFilePath);
        } catch (IOException ex) {
            log.error("Failed to delete temporary file: {}", tempFilePath, ex);
        }
    }

    private int parseCsvAndProcessInBatches(InputStream inputStream, Consumer<List<TextPair>> batchConsumer, Long datasetIdForContext, AtomicInteger totalParsedCounter) throws IOException {
        List<TextPair> currentBatch = new ArrayList<>(PARSER_BATCH_SIZE);
        int batchParsedCount = 0;

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVReader csvReader = new CSVReader(reader)) {
            String[] line;
            String[] header = csvReader.readNext();
            if (header == null) {
                 log.warn("CSV file for dataset {} is empty or has no header.", datasetIdForContext);
                 return 0;
            }

            long rowNum = 1;
            while ((line = csvReader.readNext()) != null) {
                rowNum++;
                if (line.length < 2 || line[0] == null || line[0].trim().isEmpty() || line[1] == null || line[1].trim().isEmpty()) {
                    log.warn("Skipping CSV line #{} for dataset {} due to missing text1 or text2", rowNum, datasetIdForContext);
                    continue;
                }
                TextPair textPair = TextPair.builder()
                        .text1(line[0].trim())
                        .text2(line[1].trim())
                        .metadata(line.length > 2 && line[2] != null ? line[2].trim() : null)
                        .build();
                currentBatch.add(textPair);
                batchParsedCount++;
                if (currentBatch.size() >= PARSER_BATCH_SIZE) {
                    totalParsedCounter.addAndGet(currentBatch.size());
                    batchConsumer.accept(new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            }
            if (!currentBatch.isEmpty()) {
                totalParsedCounter.addAndGet(currentBatch.size());
                batchConsumer.accept(new ArrayList<>(currentBatch));
            }
        } catch (CsvValidationException e) {
            log.error("CsvValidationException for dataset {}: {}", datasetIdForContext, e.getMessage());
            throw new IOException("Error validating CSV content: " + e.getMessage(), e);
        }
        return batchParsedCount;
    }

    private int parseXlsxAndProcessInBatches(InputStream inputStream, Consumer<List<TextPair>> batchConsumer, Long datasetIdForContext, AtomicInteger totalParsedCounter) throws IOException {
        List<TextPair> currentBatch = new ArrayList<>(PARSER_BATCH_SIZE);
        int batchParsedCount = 0;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            if (rowIterator.hasNext()) {
                rowIterator.next();
            } else {
                log.warn("XLSX sheet for dataset {} is empty or has no header.", datasetIdForContext);
                return 0;
            }

            long rowNum = 1;
            while (rowIterator.hasNext()) {
                rowNum++;
                Row row = rowIterator.next();
                Cell cell1 = row.getCell(0, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                Cell cell2 = row.getCell(1, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

                String text1 = cell1 != null ? cell1.getStringCellValue().trim() : null;
                String text2 = cell2 != null ? cell2.getStringCellValue().trim() : null;

                if (text1 == null || text1.isEmpty() || text2 == null || text2.isEmpty()) {
                    log.warn("Skipping XLSX row #{} for dataset {} due to missing text1 or text2", rowNum, datasetIdForContext);
                    continue;
                }

                String metadata = null;
                Cell cell3 = row.getCell(2, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell3 != null) {
                    metadata = cell3.getStringCellValue().trim();
                }

                TextPair textPair = TextPair.builder()
                        .text1(text1)
                        .text2(text2)
                        .metadata(metadata)
                        .build();
                currentBatch.add(textPair);
                batchParsedCount++;
                if (currentBatch.size() >= PARSER_BATCH_SIZE) {
                    totalParsedCounter.addAndGet(currentBatch.size());
                    batchConsumer.accept(new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            }
            if (!currentBatch.isEmpty()) {
                totalParsedCounter.addAndGet(currentBatch.size());
                batchConsumer.accept(new ArrayList<>(currentBatch));
            }
        } catch (Exception e) { 
            log.error("Exception during XLSX parsing for dataset {}: {}", datasetIdForContext, e.getMessage());
            throw new IOException("Error parsing XLSX content: " + e.getMessage(), e);
        }
        return batchParsedCount;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatasetResponse> getAllDatasets() {
        return datasetRepository.findAllByOrderByCreatedAtDesc().stream()
                // Pass null for the textPairsPage when listing all datasets
                .map(dataset -> mapToDatasetResponse(dataset, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DatasetResponse getDatasetResponseById(Long id, Pageable pageable) {
        Dataset dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + id));
        
        Page<TextPair> textPairs = textPairRepository.findByDatasetId(dataset.getId(), pageable);
        Page<TextPairResponse> textPairResponses = textPairs.map(this::mapToTextPairResponse);
        
        return mapToDatasetResponse(dataset, textPairResponses);
    }

    @Override
    @Transactional(readOnly = true)
    public Dataset getDatasetById(Long id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + id));
    }

    private DatasetResponse mapToDatasetResponse(Dataset dataset, /*@Nullable*/ Page<TextPairResponse> textPairsPage) {
        if (dataset == null) {
            return null;
        }
        return DatasetResponse.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .description(dataset.getDescription())
                .status(dataset.getImportStatus())
                .importErrorMessage(dataset.getImportErrorMessage())
                .originalFileName(dataset.getOriginalFilename())
                .fileSize(dataset.getFileSize())
                .fileType(dataset.getContentType())
                .totalTextPairsInFile(dataset.getTextPairCountFromFile())
                .processedTextPairsCount(dataset.getProcessedTextPairsCount())
                .failedTextPairsCount(dataset.getFailedTextPairsCount())
                .createdAt(dataset.getCreatedAt())
                .updatedAt(dataset.getUpdatedAt())
                .textPairsPage(textPairsPage)
                .build();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateDatasetImportStatus(Long datasetId, DatasetImportStatus importStatus, String importErrorMessage, Integer totalTextPairsInFile, Integer processedTextPairsCount, Integer failedTextPairsCount) {
        log.debug("Attempting to update import status for dataset ID {}: Status={}, Error='{}', Total={}, Processed={}, Failed={}",
                datasetId, importStatus, importErrorMessage, totalTextPairsInFile, processedTextPairsCount, failedTextPairsCount);
        Dataset dataset = datasetRepository.findById(datasetId).orElse(null);
        if (dataset != null) {
            dataset.setImportStatus(importStatus);
            dataset.setImportErrorMessage(importErrorMessage);
            if (totalTextPairsInFile != null) { // This is total *parsed* from file
                dataset.setTextPairCountFromFile(totalTextPairsInFile);
            }
            if (processedTextPairsCount != null) { // This is successfully *saved*
                dataset.setProcessedTextPairsCount(processedTextPairsCount);
            }
            if (failedTextPairsCount != null) { // This is *failed to save*
                dataset.setFailedTextPairsCount(failedTextPairsCount);
            }
            dataset.setUpdatedAt(LocalDateTime.now());
            datasetRepository.save(dataset);
            log.info("Successfully updated import status for dataset ID {}: Status={}, TotalParsed={}, ProcessedDb={}, FailedDb={}",
                    datasetId, importStatus, dataset.getTextPairCountFromFile(), dataset.getProcessedTextPairsCount(), dataset.getFailedTextPairsCount());
        } else {
            log.warn("Dataset with ID {} not found. Could not update import status.", datasetId);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ProcessedBatchResult saveTextPairBatch(List<TextPair> batch, Long datasetId) {
        if (batch.isEmpty()) return new ProcessedBatchResult(0, 0);

        Dataset dataset = datasetRepository.findById(datasetId)
            .orElseThrow(() -> {
                // This should ideally not happen if the main dataset creation transaction has committed.
                // If it does, it means the async process started and this method was called before the dataset was findable.
                log.error("CRITICAL: Dataset with ID {} not found during batch save within its own transaction. This indicates a severe timing or transactional issue.", datasetId);
                return new EntityNotFoundException("Dataset not found for batch save: " + datasetId);
            });

        for (TextPair tp : batch) {
            tp.setDataset(dataset); // Associate with the fetched, managed Dataset entity
        }

        try {
            textPairRepository.saveAll(batch);
            log.debug("Successfully saved batch of {} text pairs for dataset ID: {}", batch.size(), datasetId);
            return new ProcessedBatchResult(batch.size(), 0);
        } catch (DataIntegrityViolationException e) {
            // Example of more specific error handling for batch save if needed
            log.error("Data integrity violation while saving text pair batch for dataset ID: {}. Error: {}. Batch size: {}", 
                datasetId, e.getMessage(), batch.size());
            // Decide how to count errors. If saveAll fails, all are typically considered errors in that batch.
            return new ProcessedBatchResult(0, batch.size());
        } catch (Exception e) {
            log.error("Error saving text pair batch for dataset ID: {}. Error: {}. Batch size: {}. First item ID (if available): {}", 
                datasetId, e.getMessage(), batch.size(), batch.isEmpty() || batch.get(0).getId() == null ? "N/A" : batch.get(0).getId(), e);
            return new ProcessedBatchResult(0, batch.size());
        }
    }

    // Helper method to map TextPair entity to TextPairResponse DTO
    private TextPairResponse mapToTextPairResponse(TextPair textPair) {
        if (textPair == null) {
            return null;
        }
        return TextPairResponse.builder()
                .id(textPair.getId())
                .text1(textPair.getText1())
                .text2(textPair.getText2())
                .metadata(textPair.getMetadata())
                .build();
    }
}
