package com.mohamedoujdid.annotationplatform.task.model;

import com.mohamedoujdid.annotationplatform.dataset.model.Dataset;
import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet;
import com.mohamedoujdid.annotationplatform.user.model.User; // Assuming User entity exists here
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "annotation_tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AnnotationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", nullable = false)
    private Dataset dataset;
    
    // An AnnotationTask uses a LabelSet to define possible labels for its items
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_set_id", nullable = false)
    private LabelSet labelSet;

    // Status is a calculated percentage (0-100), stored to allow querying/sorting if needed.
    // It will be updated by a service method.
    @Column(nullable = false)
    @Builder.Default
    private Integer completionPercentage = 0;

    private LocalDateTime deadline;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "annotation_task_annotators",
        joinColumns = @JoinColumn(name = "annotation_task_id"),
        inverseJoinColumns = @JoinColumn(name = "annotator_id")
    )
    @Builder.Default
    private Set<User> assignedAnnotators = new HashSet<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (completionPercentage == null) {
            completionPercentage = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}