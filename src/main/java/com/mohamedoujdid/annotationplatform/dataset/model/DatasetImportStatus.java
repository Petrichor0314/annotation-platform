package com.mohamedoujdid.annotationplatform.dataset.model;

public enum DatasetImportStatus {
    PENDING,        // Initial state, awaiting file upload or processing trigger
    PENDING_UPLOAD_COMPLETION, // File uploaded, awaiting async process to confirm and start full import
    UPLOADING,      // File is being uploaded
    VALIDATING,     // File structure/format is being validated
    IMPORTING,      // Text pairs are being extracted and saved from the file
    COMPLETED,      // File processed and all text pairs imported successfully
    COMPLETED_WITH_ERRORS, // File processed, some text pairs imported, but some errors occurred
    FAILED_UPLOAD,  // File upload failed
    FAILED_VALIDATION, // File validation failed (e.g., wrong format, headers)
    FAILED_IMPORT   // An error occurred during text pair extraction or saving
} 