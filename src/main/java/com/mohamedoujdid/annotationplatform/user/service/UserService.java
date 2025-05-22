package com.mohamedoujdid.annotationplatform.user.service;

import com.mohamedoujdid.annotationplatform.user.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    void createUser(User user);
}
