package com.mohamedoujdid.annotationplatform.user.repository;

import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {
    List<Annotator> findAllByDeletedFalse();
}
