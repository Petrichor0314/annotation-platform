package com.mohamedoujdid.annotationplatform.task.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.mohamedoujdid.annotationplatform.admin.dto.dataset.DatasetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.UserAnnotationRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.task.UserAnnotationResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.user.UserSummaryResponse;
import com.mohamedoujdid.annotationplatform.annotator.dto.AnnotationPageData;
import com.mohamedoujdid.annotationplatform.task.model.AnnotationTask;

public interface AnnotationTaskService {

    AnnotationTaskResponse createTask(AnnotationTaskCreateRequest request);

    AnnotationTaskResponse getTaskResponseById(Long taskId);

    AnnotationTaskCreateRequest getTaskUpdateRequestById(Long taskId); // For populating edit form

    AnnotationTask getTaskById(Long taskId); // For internal use or when full entity is needed

    Page<AnnotationTaskResponse> getAllTasks(Pageable pageable); // Paginated

    List<AnnotationTaskResponse> getAllAnnotationTasks(); // Non-paginated for admin list

    // More specific listings, e.g., by dataset, by annotator
    Page<AnnotationTaskResponse> getTasksByDatasetId(Long datasetId, Pageable pageable);
    Page<AnnotationTaskResponse> getTasksForAnnotator(Long annotatorId, Pageable pageable);

    UserAnnotationResponse submitAnnotation(Long taskId, Long annotatorId, UserAnnotationRequest request);

    // Method to be called (e.g., scheduled or after each annotation) to update completion percentage
    void updateTaskCompletionPercentage(Long taskId);

    AnnotationTaskResponse updateTask(Long taskId, AnnotationTaskCreateRequest updateRequest); // Using CreateRequest for update for now

    void deleteTask(Long taskId);

    // Methods to provide data for creation/edit forms
    List<DatasetBasicInfoResponse> getAllDatasetsForSelection();
    List<LabelSetBasicInfoResponse> getAllLabelSetsForSelection();
    List<UserSummaryResponse> getAllAnnotatorsForSelection();

    AnnotationPageData getAnnotationPageData(Long taskId, Long annotatorId, Integer requestedTextPairIndex0Based);

    // Potential methods for managing task (update, delete, assign/unassign annotators)
    // AnnotationTaskResponse assignAnnotatorsToTask(Long taskId, Set<Long> annotatorIds);
} 