package com.mohamedoujdid.annotationplatform.task.service.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mohamedoujdid.annotationplatform.admin.dto.dataset.DatasetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetBasicInfoResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.task.AnnotationTaskResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.task.UserAnnotationRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.task.UserAnnotationResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.user.UserSummaryResponse;
import com.mohamedoujdid.annotationplatform.dataset.model.Dataset;
import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus;
import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;
import com.mohamedoujdid.annotationplatform.dataset.repository.DatasetRepository;
import com.mohamedoujdid.annotationplatform.dataset.repository.TextPairRepository;
import com.mohamedoujdid.annotationplatform.exception.ResourceNotFoundException;
import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel;
import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet;
import com.mohamedoujdid.annotationplatform.labeling.repository.ClassLabelRepository;
import com.mohamedoujdid.annotationplatform.labeling.repository.LabelSetRepository;
import com.mohamedoujdid.annotationplatform.task.model.AnnotationTask;
import com.mohamedoujdid.annotationplatform.task.model.UserAnnotation;
import com.mohamedoujdid.annotationplatform.task.repository.AnnotationTaskRepository;
import com.mohamedoujdid.annotationplatform.task.repository.UserAnnotationRepository;
import com.mohamedoujdid.annotationplatform.task.service.AnnotationTaskService;
import com.mohamedoujdid.annotationplatform.user.model.User;
import com.mohamedoujdid.annotationplatform.user.repository.AnnotatorRepository;
import com.mohamedoujdid.annotationplatform.user.repository.UserRepository;
import com.mohamedoujdid.annotationplatform.annotator.dto.AnnotationPageData;
import com.mohamedoujdid.annotationplatform.admin.dto.TextPairResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnnotationTaskServiceImpl implements AnnotationTaskService {

    private final AnnotationTaskRepository annotationTaskRepository;
    private final UserAnnotationRepository userAnnotationRepository;
    private final DatasetRepository datasetRepository;
    private final TextPairRepository textPairRepository;
    private final LabelSetRepository labelSetRepository;
    private final UserRepository userRepository;
    private final AnnotatorRepository annotatorRepository;
    private final ClassLabelRepository classLabelRepository; // For UserAnnotationResponse

    // @Lazy private final AnnotationTaskService self; // For potential @Async on updateTaskCompletionPercentage

    @Override
    @Transactional
    public AnnotationTaskResponse createTask(AnnotationTaskCreateRequest request) {
        Dataset dataset = datasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + request.getDatasetId()));
        LabelSet labelSet = labelSetRepository.findById(request.getLabelSetId())
                .orElseThrow(() -> new ResourceNotFoundException("LabelSet not found with id: " + request.getLabelSetId()));

        Set<User> annotators = new HashSet<>(userRepository.findAllById(request.getAssignedAnnotatorIds()));
        if (annotators.size() != request.getAssignedAnnotatorIds().size()) {
            log.warn("Some annotator IDs provided for task creation were not found. Requested: {}, Found: {}", request.getAssignedAnnotatorIds().size(), annotators.size());
            // Optionally, throw an exception or handle as a partial success/failure
            // For now, we proceed with found annotators.
        }
        if (annotators.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a task with no valid assigned annotators.");
        }

        AnnotationTask task = AnnotationTask.builder()
                .name(request.getName())
                .description(request.getDescription())
                .dataset(dataset)
                .labelSet(labelSet)
                .deadline(request.getDeadline())
                .assignedAnnotators(annotators)
                .completionPercentage(0) // Initial completion is 0
                .build();

        AnnotationTask savedTask = annotationTaskRepository.save(task);
        log.info("Created AnnotationTask ID: {} for Dataset ID: {} with LabelSet ID: {}", savedTask.getId(), dataset.getId(), labelSet.getId());
        // Initial completion update (will be 0% if dataset has text pairs, or 100% if no text pairs and no annotators - edge case)
        
        updateTaskCompletionPercentage(savedTask.getId()); 
        // Fetch again to get the potentially updated percentage from the call above
        AnnotationTask finalTask = annotationTaskRepository.findById(savedTask.getId()).orElse(savedTask);
        return mapToAnnotationTaskResponse(finalTask);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotationTaskResponse getTaskResponseById(Long taskId) {
        AnnotationTask task = getTaskById(taskId); // Uses internal method that fetches details
        return mapToAnnotationTaskResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotationTask getTaskById(Long taskId) {
        // Using a custom query to fetch necessary associations, adjust as needed for performance
        return annotationTaskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnotationTask not found with id: " + taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationTaskResponse> getAllTasks(Pageable pageable) {
        Page<AnnotationTask> tasksPage = annotationTaskRepository.findAll(pageable);
        return tasksPage.map(this::mapToAnnotationTaskResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationTaskResponse> getTasksByDatasetId(Long datasetId, Pageable pageable) {
        // This would require a custom query in AnnotationTaskRepository
        // e.g., findByDatasetId(Long datasetId, Pageable pageable)
        log.warn("getTasksByDatasetId not yet implemented with a specific repository query.");
        // Fallback to filtering all tasks (inefficient for large number of tasks)
        List<AnnotationTaskResponse> responses = annotationTaskRepository.findAll().stream()
            .filter(task -> task.getDataset().getId().equals(datasetId))
            .map(this::mapToAnnotationTaskResponse)
            .collect(Collectors.toList());
        // Create Page from list - for proper pagination, a direct repository query is better
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        return new PageImpl<>(responses.subList(start, end), pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnnotationTaskResponse> getTasksForAnnotator(Long annotatorId, Pageable pageable) {
        log.debug("Fetching tasks for annotator ID: {} with pageable: {}", annotatorId, pageable);
        Page<AnnotationTask> tasksPage = annotationTaskRepository.findByAssignedAnnotatorId(annotatorId, pageable);
        return tasksPage.map(this::mapToAnnotationTaskResponse);
    }

    @Override
    @Transactional
    public UserAnnotationResponse submitAnnotation(Long taskId, Long annotatorId, UserAnnotationRequest request) {
        // Validate inputs
        if (request.getTextPairId() == null || request.getClassLabelId() == null) {
            throw new IllegalArgumentException("Text pair ID and class label ID are required");
        }

        AnnotationTask task = annotationTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User annotator = userRepository.findById(annotatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Annotator not found with id: " + annotatorId));

        TextPair textPair = textPairRepository.findById(request.getTextPairId())
                .orElseThrow(() -> new ResourceNotFoundException("Text pair not found with id: " + request.getTextPairId()));

        ClassLabel classLabel = classLabelRepository.findById(request.getClassLabelId())
                .orElseThrow(() -> new ResourceNotFoundException("Class label not found with id: " + request.getClassLabelId()));

        // Check if annotation already exists
        UserAnnotation existingAnnotation = userAnnotationRepository
                .findByAnnotationTaskIdAndAnnotatorIdAndTextPairId(
                        taskId, annotatorId, request.getTextPairId())
                .orElse(null);

        UserAnnotation annotation;
        if (existingAnnotation != null) {
            // Update existing annotation
            existingAnnotation.setClassLabel(classLabel);
            // If you have a timestamp field, update it here
            // existingAnnotation.setUpdatedAt(LocalDateTime.now());
            annotation = userAnnotationRepository.save(existingAnnotation);
            log.info("Updated existing annotation for task {}, annotator {}, text pair {}", taskId, annotatorId, request.getTextPairId());
        } else {
            // Create new annotation
            annotation = UserAnnotation.builder()
                    .annotationTask(task)
                    .annotator(annotator)
                    .textPair(textPair)
                    .classLabel(classLabel)
                    // If you have timestamp fields, set them here
                    // .createdAt(LocalDateTime.now())
                    // .updatedAt(LocalDateTime.now())
                    .build();
            annotation = userAnnotationRepository.save(annotation);
            log.info("Created new annotation for task {}, annotator {}, text pair {}", taskId, annotatorId, request.getTextPairId());
        }

        // Update the last annotated text pair ID for this annotator
        task.getLastAnnotatedPairIds().put(annotatorId, textPair.getId());
        
        // Update annotator progress
        List<TextPair> allTextPairsInDataset = textPairRepository.findByDatasetIdOrderByIdAsc(task.getDataset().getId());
        List<UserAnnotation> userAnnotationsForTask = userAnnotationRepository.findByAnnotationTaskIdAndAnnotatorId(taskId, annotatorId);
        
        int totalPairs = allTextPairsInDataset.size();
        int annotatedCount = userAnnotationsForTask.size();
        int progressPercentage = totalPairs > 0 ? (annotatedCount * 100 / totalPairs) : 0;
        
        // Update the annotator's progress percentage
        task.getAnnotatorProgressPercentages().put(annotatorId, progressPercentage);
        
        // Save the updated task
        annotationTaskRepository.save(task);
        
        // Update the overall task completion percentage
        updateTaskCompletionPercentage(taskId);

        // Create response object
        return UserAnnotationResponse.builder()
                .id(annotation.getId())
                .textPairId(annotation.getTextPair().getId())
                .classLabelId(annotation.getClassLabel().getId())
                .annotatorId(annotation.getAnnotator().getId())
                // If your response object has a taskId field
                // .taskId(annotation.getAnnotationTask().getId())
                // If you have timestamp fields in your response
                // .createdAt(annotation.getCreatedAt())
                // .updatedAt(annotation.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void updateTaskCompletionPercentage(Long taskId) {
        AnnotationTask task = annotationTaskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnotationTask not found with id: " + taskId));

        Set<User> assignedAnnotators = task.getAssignedAnnotators();
        List<TextPair> textPairsInDataset = textPairRepository.findByDatasetIdOrderByIdAsc(task.getDataset().getId());

        if (assignedAnnotators.isEmpty() || textPairsInDataset.isEmpty()) {
            task.setCompletionPercentage(assignedAnnotators.isEmpty() && textPairsInDataset.isEmpty() ? 100 : 0);
            annotationTaskRepository.save(task);
            log.info("Task ID: {} completion updated to {}% (no annotators or no text pairs).", taskId, task.getCompletionPercentage());
            return;
        }

        int totalTextPairs = textPairsInDataset.size();
        int totalPossibleAnnotations = totalTextPairs * assignedAnnotators.size();
        
        // Simply count the total number of annotations made for this task
        long totalAnnotationsMade = userAnnotationRepository.countByAnnotationTaskId(taskId);
        
        // Calculate the overall progress percentage
        int overallPercentage = totalPossibleAnnotations > 0 
            ? (int) Math.round(((double) totalAnnotationsMade / totalPossibleAnnotations) * 100.0) 
            : 0;
        
        task.setCompletionPercentage(overallPercentage);
        annotationTaskRepository.save(task);
        
        log.info("Task ID: {} overall completion updated to {}%. Total annotations: {}/{}", 
                 taskId, overallPercentage, totalAnnotationsMade, totalPossibleAnnotations);
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotationTaskCreateRequest getTaskUpdateRequestById(Long taskId) {
        AnnotationTask task = getTaskById(taskId);
        return mapToAnnotationTaskCreateRequest(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnnotationTaskResponse> getAllAnnotationTasks() {
        // Consider if findByIdWithDetails is needed for all or if a simpler query suffices
        // If mapToAnnotationTaskResponse requires details not in a simple findAll(), this might lead to N+1
        // For now, using findAll and relying on mapToAnnotationTaskResponse to fetch or have details
        return annotationTaskRepository.findAll().stream()
                .map(this::mapToAnnotationTaskResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AnnotationTaskResponse updateTask(Long taskId, AnnotationTaskCreateRequest request) {
        AnnotationTask task = getTaskById(taskId);

        Dataset dataset = datasetRepository.findById(request.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("Dataset not found with id: " + request.getDatasetId()));
        LabelSet labelSet = labelSetRepository.findById(request.getLabelSetId())
                .orElseThrow(() -> new ResourceNotFoundException("LabelSet not found with id: " + request.getLabelSetId()));
        Set<User> annotators = new HashSet<>(userRepository.findAllById(request.getAssignedAnnotatorIds()));

        if (annotators.isEmpty()) {
            throw new IllegalArgumentException("Cannot update a task to have no assigned annotators.");
        }

        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setDataset(dataset);
        task.setLabelSet(labelSet);
        task.setDeadline(request.getDeadline());
        task.setAssignedAnnotators(annotators);
        // Completion percentage will be recalculated based on new dataset/annotators if necessary

        AnnotationTask updatedTask = annotationTaskRepository.save(task);
        log.info("Updated AnnotationTask ID: {}", updatedTask.getId());
        // Recalculate completion percentage as dataset or annotators might have changed
        updateTaskCompletionPercentage(updatedTask.getId()); 
        AnnotationTask finalTask = annotationTaskRepository.findById(updatedTask.getId()).orElse(updatedTask);
        return mapToAnnotationTaskResponse(finalTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        AnnotationTask task = annotationTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("AnnotationTask not found with id: " + taskId));
        // Consider related entities: UserAnnotations for this task should also be deleted or handled.
        // For now, simple deletion of the task itself.
        // If UserAnnotations have a foreign key constraint, they might need to be deleted first
        // or cascade delete should be configured on the AnnotationTask entity.
        userAnnotationRepository.deleteByAnnotationTaskId(taskId); // Assuming this method exists or is added
        annotationTaskRepository.delete(task);
        log.info("Deleted AnnotationTask ID: {}", taskId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatasetBasicInfoResponse> getAllDatasetsForSelection() {
        // Filter datasets to include only those that are COMPLETED
        return datasetRepository.findByImportStatus(DatasetImportStatus.COMPLETED).stream()
                .map(dataset -> DatasetBasicInfoResponse.builder()
                        .id(dataset.getId())
                        .name(dataset.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelSetBasicInfoResponse> getAllLabelSetsForSelection() {
        return labelSetRepository.findAll().stream()
                .map(labelSet -> LabelSetBasicInfoResponse.builder()
                        .id(labelSet.getId())
                        .name(labelSet.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllAnnotatorsForSelection() {
        // Fetch all annotators and filter by !isDeleted (or isEnabled)
        return annotatorRepository.findAll().stream()
                .filter(User::isEnabled) // or !user.isDeleted()
                .map(annotator -> UserSummaryResponse.builder()
                        .id(annotator.getId())
                        .email(annotator.getEmail())
                        .firstName(annotator.getFirstName())
                        .lastName(annotator.getLastName())
                        .roleName(annotator.getRole() != null ? annotator.getRole().getName() : "N/A")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AnnotationPageData getAnnotationPageData(Long taskId, Long annotatorId, Integer requestedTextPairIndex0Based) {
        // Validate task and annotator exist and are related
        AnnotationTask task = annotationTaskRepository.findByIdWithDetails(taskId) 
                .orElseThrow(() -> new ResourceNotFoundException("AnnotationTask not found with id: " + taskId));

        User annotator = userRepository.findById(annotatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Annotator not found with id: " + annotatorId));

        if (task.getAssignedAnnotators().stream().noneMatch(a -> a.getId().equals(annotatorId))) {
            log.warn("Security Alert: Annotator {} attempted to access task {} they are not assigned to.", annotatorId, taskId);
            throw new SecurityException("User " + annotatorId + " is not assigned to task " + taskId);
        }

        // Get all text pairs in order
        List<TextPair> allTextPairsInDataset = textPairRepository.findByDatasetIdOrderByIdAsc(task.getDataset().getId());
        if (allTextPairsInDataset.isEmpty()) {
            log.warn("Task {} (Dataset {}) has no text pairs.", taskId, task.getDataset().getId());
            return AnnotationPageData.builder()
                    .taskDetails(mapToAnnotationTaskResponse(task))
                    .availableClassLabels(Collections.emptyList())
                    .currentPageNumber(0)
                    .totalTextPairPages(0)
                    .allPairsInTaskAnnotatedByCurrentUser(true)
                    .currentTextPairIndexInDataset(-1)
                    .build();
        }

        // Get all annotations by this user for this task
        List<UserAnnotation> userAnnotationsForTask = userAnnotationRepository.findByAnnotationTaskIdAndAnnotatorId(taskId, annotatorId);
        
        // Create a set of annotated text pair IDs for quick lookup
        Set<Long> annotatedTextPairIds = userAnnotationsForTask.stream()
                .map(ua -> ua.getTextPair().getId())
                .collect(Collectors.toSet());
        
        // Calculate progress statistics
        int totalPairs = allTextPairsInDataset.size();
        int annotatedCount = annotatedTextPairIds.size();
        int progressPercentage = totalPairs > 0 ? (annotatedCount * 100 / totalPairs) : 0;
        
        // Update the annotator's progress in the task
        task.getAnnotatorProgressPercentages().put(annotatorId, progressPercentage);
        annotationTaskRepository.save(task);
        
        // Create a map of text pair ID to annotation for quick lookup of previous selections
        Map<Long, UserAnnotation> textPairToAnnotationMap = userAnnotationsForTask.stream()
                .collect(Collectors.toMap(
                    ua -> ua.getTextPair().getId(),
                    ua -> ua,
                    (existing, replacement) -> existing // Keep existing in case of duplicates
                ));

        // Determine which text pair to show
        int currentPairIndex;
        TextPair currentTextPairEntity;
        
        // If a specific index is requested and valid, use it
        if (requestedTextPairIndex0Based != null && requestedTextPairIndex0Based >= 0 && requestedTextPairIndex0Based < allTextPairsInDataset.size()) {
            currentPairIndex = requestedTextPairIndex0Based;
            currentTextPairEntity = allTextPairsInDataset.get(currentPairIndex);
        } else {
            // No specific index requested, determine where to start
            Long lastAnnotatedPairId = task.getLastAnnotatedPairIds().get(annotatorId);
            
            if (lastAnnotatedPairId != null) {
                // Find the index of the last annotated pair
                int lastAnnotatedIndex = -1;
                for (int i = 0; i < allTextPairsInDataset.size(); i++) {
                    if (allTextPairsInDataset.get(i).getId().equals(lastAnnotatedPairId)) {
                        lastAnnotatedIndex = i;
                        break;
                    }
                }
                
                if (lastAnnotatedIndex != -1) {
                    // If we found the last annotated pair, start from there
                    // This allows the user to review their last annotation
                    currentPairIndex = lastAnnotatedIndex;
                } else {
                    // Last annotated pair not found in dataset (unusual case)
                    // Find first unannotated pair
                    currentPairIndex = findFirstUnannotatedPairIndex(allTextPairsInDataset, annotatedTextPairIds);
                    if (currentPairIndex == -1) {
                        // All pairs annotated, start from the beginning
                        currentPairIndex = 0;
                    }
                }
            } else {
                // No last annotated pair recorded, find first unannotated pair
                currentPairIndex = findFirstUnannotatedPairIndex(allTextPairsInDataset, annotatedTextPairIds);
                if (currentPairIndex == -1) {
                    // All pairs annotated, start from the beginning
                    currentPairIndex = 0;
                }
            }
            
            currentTextPairEntity = allTextPairsInDataset.get(currentPairIndex);
        }
        
        // Map the current text pair to a DTO
        TextPairResponse currentTextPairDto = mapToTextPairResponse(currentTextPairEntity);
        
        // Get the previously selected label for this text pair, if any
        Long previouslySelectedLabelId = Optional.ofNullable(textPairToAnnotationMap.get(currentTextPairEntity.getId()))
                .map(ua -> ua.getClassLabel().getId())
                .orElse(null);
                
        // Get all available class labels for this task
        List<ClassLabelResponse> classLabelDtos = task.getLabelSet().getClassLabels().stream()
                .map(this::mapToClassLabelResponse)
                .collect(Collectors.toList());
                
        // Calculate next and previous text pair IDs for navigation
        Long nextTpId = (currentPairIndex + 1 < allTextPairsInDataset.size()) ? allTextPairsInDataset.get(currentPairIndex + 1).getId() : null;
        Long prevTpId = (currentPairIndex - 1 >= 0) ? allTextPairsInDataset.get(currentPairIndex - 1).getId() : null;

        // Build and return the response
        return AnnotationPageData.builder()
                .taskDetails(mapToAnnotationTaskResponse(task))
                .currentTextPair(currentTextPairDto)
                .availableClassLabels(classLabelDtos)
                .currentPageNumber(currentPairIndex + 1) 
                .totalTextPairPages(allTextPairsInDataset.size()) 
                .previouslySelectedClassLabelId(previouslySelectedLabelId)
                .allPairsInTaskAnnotatedByCurrentUser(annotatedCount == totalPairs)
                .nextTextPairId(nextTpId)
                .previousTextPairId(prevTpId)
                .currentTextPairIndexInDataset(currentPairIndex)
                .annotatorProgressPercentage(progressPercentage)
                .annotatorCompletedPairs(annotatedCount)
                .totalPairsInTask(totalPairs)
                .build();
    }

    private int findFirstUnannotatedPairIndex(List<TextPair> allPairs, Set<Long> annotatedTextPairIds) {
        for (int i = 0; i < allPairs.size(); i++) {
            if (!annotatedTextPairIds.contains(allPairs.get(i).getId())) {
                return i;
            }
        }
        return -1; // All pairs annotated
    }
    
    private TextPairResponse mapToTextPairResponse(TextPair textPair) {
        if (textPair == null) return null;
        return TextPairResponse.builder()
                .id(textPair.getId())
                .text1(textPair.getText1())
                .text2(textPair.getText2())
                .metadata(textPair.getMetadata())
                .build();
    }

    private ClassLabelResponse mapToClassLabelResponse(ClassLabel classLabel) {
        if (classLabel == null) return null;
        return ClassLabelResponse.builder()
                .id(classLabel.getId())
                .name(classLabel.getName())
                .value(classLabel.getValue())
                .build();
    }

    private AnnotationTaskCreateRequest mapToAnnotationTaskCreateRequest(AnnotationTask task) {
        if (task == null) return null;
        return AnnotationTaskCreateRequest.builder()
                .name(task.getName())
                .description(task.getDescription())
                .datasetId(task.getDataset().getId())
                .labelSetId(task.getLabelSet().getId())
                .assignedAnnotatorIds(task.getAssignedAnnotators().stream().map(User::getId).collect(Collectors.toSet()))
                .deadline(task.getDeadline())
                .build();
    }

    // --- Helper Mappers ---
    private AnnotationTaskResponse mapToAnnotationTaskResponse(AnnotationTask task) {
        if (task == null) return null;

        Set<UserSummaryResponse> annotatorSummaries = task.getAssignedAnnotators().stream()
                .map(user -> UserSummaryResponse.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .roleName(user.getRole() != null ? user.getRole().getName() : "N/A")
                        .build())
                .collect(Collectors.toSet());
        
        // Fetch text pairs for the dataset to get an accurate count
        List<TextPair> textPairsInDataset = textPairRepository.findByDatasetIdOrderByIdAsc(task.getDataset().getId());
        int totalTextPairs = textPairsInDataset.size();
        
        int completedPairsApproximation = (int) Math.round((task.getCompletionPercentage() / 100.0) * totalTextPairs);

        return AnnotationTaskResponse.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .datasetId(task.getDataset().getId())
                .datasetName(task.getDataset().getName())
                .labelSetId(task.getLabelSet().getId())
                .labelSetName(task.getLabelSet().getName())
                .completionPercentage(task.getCompletionPercentage())
                .deadline(task.getDeadline())
                .assignedAnnotators(annotatorSummaries)
                .totalTextPairsInTask(totalTextPairs)
                .completedTextPairsInTask(completedPairsApproximation)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    private UserAnnotationResponse mapToUserAnnotationResponse(UserAnnotation annotation) {
        if (annotation == null) return null;
        return UserAnnotationResponse.builder()
                .id(annotation.getId())
                .annotationTaskId(annotation.getAnnotationTask().getId())
                .textPairId(annotation.getTextPair().getId())
                .classLabelId(annotation.getClassLabel().getId())
                .classLabelName(annotation.getClassLabel().getName())
                .classLabelValue(annotation.getClassLabel().getValue())
                .annotatorId(annotation.getAnnotator().getId())
                .annotatorName(annotation.getAnnotator().getFirstName() + " " + annotation.getAnnotator().getLastName())
                .createdAt(annotation.getCreatedAt())
                .build();
    }
} 