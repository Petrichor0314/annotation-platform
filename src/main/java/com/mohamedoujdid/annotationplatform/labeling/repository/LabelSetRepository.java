package com.mohamedoujdid.annotationplatform.labeling.repository;

import com.mohamedoujdid.annotationplatform.labeling.model.LabelSet; // Points to labeling.model
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabelSetRepository extends JpaRepository<LabelSet, Long> {
} 