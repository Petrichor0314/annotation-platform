package com.mohamedoujdid.annotationplatform.task.model;

import java.time.LocalDateTime; // Forward declaration

import com.mohamedoujdid.annotationplatform.annotation.model.Annotation;
import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;
import com.mohamedoujdid.annotationplatform.task.model.enums.TaskItemStatus;
import com.mohamedoujdid.annotationplatform.user.model.Annotator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TaskItem {
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
    @JoinColumn(name = "annotator_id", nullable = false) // The specific annotator assigned this item
    private Annotator annotator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskItemStatus status;

    @OneToOne(mappedBy = "taskItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Annotation annotation; // The result of this task item

    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;

    @PrePersist
    protected void onPersist() {
        if (assignedAt == null) assignedAt = LocalDateTime.now();
    }
}