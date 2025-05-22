package com.mohamedoujdid.annotationplatform.annotation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mohamedoujdid.annotationplatform.annotation.model.Annotation;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {
}