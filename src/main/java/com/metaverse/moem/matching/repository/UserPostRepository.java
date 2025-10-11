package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserPostRepository extends JpaRepository<UserPost, Long> {
    boolean existsByAuth_Email(String email);
    boolean existsByAuth_Username(String username);
    Optional<UserPost> findByAuth_Id(Long authId);
    Optional<UserPost> findByAuth_Username(String username);
}
