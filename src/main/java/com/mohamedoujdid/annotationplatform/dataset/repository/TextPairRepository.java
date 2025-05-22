package com.mohamedoujdid.annotationplatform.dataset.repository;

import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TextPairRepository extends JpaRepository<TextPair, Long> {
    Page<TextPair> findByDatasetId(Long datasetId, Pageable pageable);
    List<TextPair> findByDatasetIdOrderByIdAsc(Long datasetId);
}
