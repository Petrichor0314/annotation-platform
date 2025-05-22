package com.mohamedoujdid.annotationplatform.task.model;

import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;
import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel;
import com.mohamedoujdid.annotationplatform.user.model.User; // Assuming User entity
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_annotations",
    uniqueConstraints = {
        // Ensures one annotation per annotator per text_pair per task
        @UniqueConstraint(columnNames = {"annotation_task_id", "text_pair_id", "annotator_id"})
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_task_id", nullable = false)
    private AnnotationTask annotationTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "text_pair_id", nullable = false)
    private TextPair textPair;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_label_id", nullable = false)
    private ClassLabel classLabel; // The label chosen by the annotator

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotator_id", nullable = false)
    private User annotator; // The user who made this annotation

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 