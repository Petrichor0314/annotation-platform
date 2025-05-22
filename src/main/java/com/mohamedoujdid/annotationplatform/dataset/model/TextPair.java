package com.mohamedoujdid.annotationplatform.dataset.model;

import java.time.LocalDateTime;

import com.mohamedoujdid.annotationplatform.dataset.model.Dataset; // Points to dataset domain

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "text_pairs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TextPair {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;

    @Column(columnDefinition="LONGTEXT", nullable = false)
    private String text1;

    @Column(columnDefinition="LONGTEXT", nullable = false)
    private String text2;

    @Column(columnDefinition="LONGTEXT") // For JSON or other structured text
    private String metadata;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 