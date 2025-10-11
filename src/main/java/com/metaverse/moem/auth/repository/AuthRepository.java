package com.metaverse.moem.auth.repository;

import com.metaverse.moem.auth.domain.Auth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {
    Optional<Auth> findByEmail(String email);
    Optional<Auth> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
