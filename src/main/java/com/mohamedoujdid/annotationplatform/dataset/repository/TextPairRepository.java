package com.mohamedoujdid.annotationplatform.dataset.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mohamedoujdid.annotationplatform.dataset.model.TextPair;

@Repository
public interface TextPairRepository extends JpaRepository<TextPair, Long> {
    Page<TextPair> findByDatasetId(Long datasetId, Pageable pageable);
    List<TextPair> findByDatasetIdOrderByIdAsc(Long datasetId);
    void deleteAllByDatasetId(Long datasetId);
}
