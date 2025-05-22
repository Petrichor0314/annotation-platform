package com.mohamedoujdid.annotationplatform.task.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mohamedoujdid.annotationplatform.task.model.TaskItem;

public interface TaskItemRepository extends JpaRepository<TaskItem, Long> {
    List<TaskItem> findByAnnotationTaskId(Long taskId);
    List<TaskItem> findByAnnotatorIdAndAnnotationTaskId(Long annotatorId, Long taskId);
    List<TaskItem> findByAnnotatorIdAndStatus(Long annotatorId, com.mohamedoujdid.annotationplatform.task.model.enums.TaskItemStatus status);
}