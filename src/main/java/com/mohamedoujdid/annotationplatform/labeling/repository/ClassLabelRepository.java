package com.mohamedoujdid.annotationplatform.labeling.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mohamedoujdid.annotationplatform.labeling.model.ClassLabel; // Points to labeling.model

public interface ClassLabelRepository extends JpaRepository<ClassLabel, Long> {
    List<ClassLabel> findByLabelSetId(Long labelSetId);
} 