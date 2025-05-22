package com.mohamedoujdid.annotationplatform.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "annotators")
public class Annotator extends User {
    public Annotator() {
        super();
    }

    public Annotator(String email, String password, Role role, boolean locked, boolean passwordChangeRequired) {
        super(email, password, role, locked, passwordChangeRequired);
    }
}