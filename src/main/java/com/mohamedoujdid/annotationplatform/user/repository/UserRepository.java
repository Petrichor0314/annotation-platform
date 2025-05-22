package com.mohamedoujdid.annotationplatform.user.repository;

import com.mohamedoujdid.annotationplatform.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAllByDeletedFalse(Pageable pageable);
    List<User> findAllByDeletedFalse();

}