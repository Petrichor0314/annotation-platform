package com.mohamedoujdid.annotationplatform.dataset.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mohamedoujdid.annotationplatform.dataset.model.Dataset;
import com.mohamedoujdid.annotationplatform.dataset.model.DatasetImportStatus;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, Long> {
    List<Dataset> findAllByOrderByCreatedAtDesc();
    boolean existsByNameIgnoreCase(String name);
    List<Dataset> findByImportStatus(DatasetImportStatus importStatus);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
