package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
