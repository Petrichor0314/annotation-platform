package com.mohamedoujdid.annotationplatform.annotation.model;

import java.time.LocalDateTime; // The specific chosen label

import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel;
import com.mohamedoujdid.annotationplatform.task.model.TaskItem;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
// If you allow multi-label selection for a single annotation:
// import java.util.Set;
// import java.util.HashSet;

@Entity
@Table(name = "annotations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_item_id", nullable = false, unique = true) // Each task item results in one annotation
    private TaskItem taskItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chosen_class_label_id", nullable = false) // For single-label classification
    private ClassLabel chosenClassLabel;

    /*
    // If multi-label classification is allowed for an annotation:
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "annotation_chosen_labels",
        joinColumns = @JoinColumn(name = "annotation_id"),
        inverseJoinColumns = @JoinColumn(name = "class_label_id")
    )
    private Set<ClassLabel> chosenClassLabels = new HashSet<>();
    */

    @Lob
    private String notes; // Annotator's optional notes

    private LocalDateTime annotationTime;

    @PrePersist
    protected void onPersist() {
        if (annotationTime == null) annotationTime = LocalDateTime.now();
    }
}