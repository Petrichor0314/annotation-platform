package com.mohamedoujdid.annotationplatform.labeling.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.ArrayList;

import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel; // Points to labeling domain

@Entity
@Table(name = "label_sets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LabelSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Lob
    private String description;

    @OneToMany(mappedBy = "labelSet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ClassLabel> classLabels = new ArrayList<>();
} 