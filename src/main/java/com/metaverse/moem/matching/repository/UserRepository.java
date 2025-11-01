package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
}
