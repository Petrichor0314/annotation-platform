package com.mohamedoujdid.annotationplatform.labeling.service;

import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.ClassLabelResponse;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetCreateRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.labeling.LabelSetResponse;
import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel;
import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet;

import java.util.List;

public interface LabelingService {
    // LabelSet methods
    LabelSet createLabelSet(LabelSetCreateRequest request);
    List<LabelSetResponse> getAllLabelSets();
    LabelSetResponse getLabelSetResponseById(Long id);
    LabelSet getLabelSetById(Long id);

    // ClassLabel methods
    ClassLabel createClassLabel(Long labelSetId, ClassLabelCreateRequest request);
    List<ClassLabelResponse> getClassLabelsBySetId(Long labelSetId);
} 