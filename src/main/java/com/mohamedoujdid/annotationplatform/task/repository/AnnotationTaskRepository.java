package com.mohamedoujdid.annotationplatform.task.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mohamedoujdid.annotationplatform.task.model.AnnotationTask;

@Repository
public interface AnnotationTaskRepository extends JpaRepository<AnnotationTask, Long> {

    // Custom query to fetch task with annotators and dataset to avoid N+1 issues if needed frequently
    @Query("SELECT DISTINCT t FROM AnnotationTask t LEFT JOIN FETCH t.assignedAnnotators LEFT JOIN FETCH t.dataset d LEFT JOIN FETCH d.textPairs WHERE t.id = :taskId")
    Optional<AnnotationTask> findByIdWithDetails(@Param("taskId") Long taskId);

    @Query("SELECT DISTINCT t FROM AnnotationTask t JOIN t.assignedAnnotators u WHERE u.id = :annotatorId ORDER BY t.createdAt DESC")
    Page<AnnotationTask> findByAssignedAnnotatorId(@Param("annotatorId") Long annotatorId, Pageable pageable);
    
    // Added methods for dashboard
    long countByCompletionPercentageLessThan(int percentage);
    long countByCompletionPercentage(int percentage);
    long countByCompletionPercentageBetween(int minPercentage, int maxPercentage);
}