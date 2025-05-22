package com.mohamedoujdid.annotationplatform.labeling.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet; // Points to labeling domain

@Entity
@Table(name = "class_labels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClassLabel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_set_id", nullable = false)
    private LabelSet labelSet;

    @Column(nullable = false)
    private String name;

    private String value;

    @Lob
    private String description;
} 