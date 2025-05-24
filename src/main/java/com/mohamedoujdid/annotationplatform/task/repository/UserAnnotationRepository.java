package com.mohamedoujdid.annotationplatform.task.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mohamedoujdid.annotationplatform.task.model.UserAnnotation;

@Repository
public interface UserAnnotationRepository extends JpaRepository<UserAnnotation, Long> {

    // Find annotations for a specific task and text pair
    List<UserAnnotation> findByAnnotationTaskIdAndTextPairId(Long annotationTaskId, Long textPairId);

    List<UserAnnotation> findByAnnotationTaskIdAndAnnotatorId(Long annotationTaskId, Long annotatorId);
    
    // Find annotations for a specific task with pagination
    Page<UserAnnotation> findByAnnotationTaskId(Long annotationTaskId, Pageable pageable);
    
    // Find all annotations for a specific task without pagination
    List<UserAnnotation> findByAnnotationTaskId(Long annotationTaskId);
    
    // Find annotations for a specific task and annotator with pagination
    Page<UserAnnotation> findByAnnotationTaskIdAndAnnotatorId(Long annotationTaskId, Long annotatorId, Pageable pageable);

    // Count annotations by an annotator for a specific task
    long countByAnnotationTaskIdAndAnnotatorId(Long annotationTaskId, Long annotatorId);

    // Count all annotations for a specific task
    long countByAnnotationTaskId(Long annotationTaskId);

    // Count distinct text pairs annotated for a task (at least one annotation)
    @Query("SELECT COUNT(DISTINCT ua.textPair.id) FROM UserAnnotation ua WHERE ua.annotationTask.id = :taskId")
    long countDistinctTextPairsAnnotatedForTask(@Param("taskId") Long taskId);

    // Count annotations for a specific text_pair within a specific task by a set of annotators
    @Query("SELECT COUNT(ua) FROM UserAnnotation ua " +
           "WHERE ua.annotationTask.id = :taskId AND ua.textPair.id = :textPairId AND ua.annotator.id IN :annotatorIds")
    long countAnnotationsForTextPairByAnnotators(
            @Param("taskId") Long taskId, 
            @Param("textPairId") Long textPairId, 
            @Param("annotatorIds") Set<Long> annotatorIds);

    // Find all annotations for a given task, optionally fetching related entities if needed for specific views
    @Query("SELECT ua FROM UserAnnotation ua LEFT JOIN FETCH ua.classLabel WHERE ua.annotationTask.id = :taskId")
    List<UserAnnotation> findAllByAnnotationTaskIdWithDetails(@Param("taskId") Long taskId);

    void deleteByAnnotationTaskId(Long annotationTaskId);
    Optional<UserAnnotation> findByAnnotationTaskIdAndAnnotatorIdAndTextPairId(Long taskId, Long annotatorId, Long textPairId);
} 