package com.mohamedoujdid.annotationplatform.user.repository;

import com.mohamedoujdid.annotationplatform.user.model.Annotator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnotatorRepository extends JpaRepository<Annotator, Long> {}
