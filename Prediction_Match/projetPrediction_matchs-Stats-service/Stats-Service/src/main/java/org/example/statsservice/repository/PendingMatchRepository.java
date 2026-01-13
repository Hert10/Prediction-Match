package org.example.statsservice.repository;

import org.example.statsservice.entity.PendingMatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingMatchRepository extends JpaRepository<PendingMatch, Long> {
    boolean existsByMatchId(Long matchId);
}
