package com.mohamedoujdid.annotationplatform.dataset.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;   // Within dataset domain
import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus; // New import

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "datasets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Dataset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TextPair> textPairs = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DatasetImportStatus importStatus = DatasetImportStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String importErrorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Fields to store original file info (optional, but good practice)
    private String originalFilename;
    private String storedFilename; // e.g., if you rename it on disk
    private Long fileSize;
    private String contentType;
    private Integer textPairCountFromFile; // Number of pairs detected in the file during import
    @Builder.Default
    private Integer processedTextPairsCount = 0;
    @Builder.Default
    private Integer failedTextPairsCount = 0;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (importStatus == null) {
            importStatus = DatasetImportStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 