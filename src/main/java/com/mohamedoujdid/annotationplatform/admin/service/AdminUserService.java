package com.mohamedoujdid.annotationplatform.admin.service;

import java.util.List;

import com.mohamedoujdid.annotationplatform.admin.dto.user.CreateUserRequest;
import com.mohamedoujdid.annotationplatform.admin.dto.user.UpdateUserRequest;
import com.mohamedoujdid.annotationplatform.user.model.Role;
import com.mohamedoujdid.annotationplatform.user.model.User;

public interface AdminUserService {
    List<User> getAllUsers();
    void createUser(CreateUserRequest form);
    UpdateUserRequest getUpdateUserForm(Long id);
    void updateUser(Long id, UpdateUserRequest form, Role role);
    void deleteUser(Long id);
}
