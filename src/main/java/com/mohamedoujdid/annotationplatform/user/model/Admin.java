package com.mohamedoujdid.annotationplatform.user.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "admins")
public class Admin extends User {
    public Admin() {
        super();
    }

    public Admin(String email, String password, Role role, boolean locked, boolean passwordChangeRequired) {
        super(email, password, role, locked, passwordChangeRequired);
    }
}
