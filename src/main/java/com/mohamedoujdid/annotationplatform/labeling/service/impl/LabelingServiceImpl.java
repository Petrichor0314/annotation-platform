package com.mohamedoujdid.annotationplatform.labeling.service.impl;

import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetResponse;
import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel;
import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet;
import com.mohamedoujdid.annotationplatform.labeling.repository.ClassLabelRepository;
import com.mohamedoujdid.annotationplatform.labeling.repository.LabelSetRepository;
import com.mohamedoujdid.annotationplatform.labeling.service.LabelingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelingServiceImpl implements LabelingService {

    private final LabelSetRepository labelSetRepository;
    private final ClassLabelRepository classLabelRepository;

    // LabelSet methods
    @Override
    @Transactional
    public LabelSet createLabelSet(LabelSetCreateRequest request) {
        LabelSet labelSet = LabelSet.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return labelSetRepository.save(labelSet);
    }

    @Override
    public List<LabelSetResponse> getAllLabelSets() {
        return labelSetRepository.findAll().stream()
                .map(this::mapToLabelSetResponse)
                .collect(Collectors.toList());
    }

    @Override
    public LabelSetResponse getLabelSetResponseById(Long id) {
        LabelSet labelSet = getLabelSetById(id);
        return mapToLabelSetResponse(labelSet);
    }

    @Override
    public LabelSet getLabelSetById(Long id) {
        return labelSetRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("LabelSet not found with id: " + id));
    }

    // ClassLabel methods
    @Override
    @Transactional
    public ClassLabel createClassLabel(Long labelSetId, ClassLabelCreateRequest request) {
        LabelSet labelSet = getLabelSetById(labelSetId); // Ensures LabelSet exists

        ClassLabel classLabel = ClassLabel.builder()
                .name(request.getName())
                .value(request.getValue())
                .description(request.getDescription())
                .labelSet(labelSet)
                .build();
        return classLabelRepository.save(classLabel);
    }

    @Override
    public List<ClassLabelResponse> getClassLabelsBySetId(Long labelSetId) {
        // Ensure LabelSet exists before fetching its labels
        if (!labelSetRepository.existsById(labelSetId)) {
            throw new EntityNotFoundException("LabelSet not found with id: " + labelSetId + " when fetching class labels.");
        }
        return classLabelRepository.findByLabelSetId(labelSetId).stream()
                .map(this::mapToClassLabelResponse)
                .collect(Collectors.toList());
    }

    // Helper mappers
    private LabelSetResponse mapToLabelSetResponse(LabelSet labelSet) {
        List<ClassLabelResponse> classLabelResponses = labelSet.getClassLabels() != null ?
                labelSet.getClassLabels().stream()
                        .map(this::mapToClassLabelResponse)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return LabelSetResponse.builder()
                .id(labelSet.getId())
                .name(labelSet.getName())
                .description(labelSet.getDescription())
                .classLabels(classLabelResponses)
                .classLabelCount(classLabelResponses.size())
                .build();
    }

    private ClassLabelResponse mapToClassLabelResponse(ClassLabel classLabel) {
        return ClassLabelResponse.builder()
                .id(classLabel.getId())
                .name(classLabel.getName())
                .value(classLabel.getValue())
                .description(classLabel.getDescription())
                .labelSetId(classLabel.getLabelSet() != null ? classLabel.getLabelSet().getId() : null)
                .build();
    }
} 