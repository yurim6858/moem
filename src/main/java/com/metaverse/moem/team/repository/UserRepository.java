package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
