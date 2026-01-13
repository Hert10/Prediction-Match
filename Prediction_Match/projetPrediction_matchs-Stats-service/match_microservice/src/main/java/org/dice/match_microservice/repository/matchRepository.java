package org.dice.match_microservice.repository;

import org.dice.match_microservice.entity.Match;
import org.dice.match_microservice.entity.MatchStatus;
import org.reactivestreams.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface matchRepository extends JpaRepository<Match, Long> {
    Optional<Match> findByMatchId(String matchId);
    List<Match> findByStatus(MatchStatus matchStatus);
    Optional<Match> findTopByStatusOrderByMatchDateDesc(MatchStatus status);
    Optional<Match> findTopByStatusOrderByMatchDateAsc(MatchStatus status);
    List<Match> findByMatchDateBetweenAndStatus(LocalDateTime startOfToday, LocalDateTime endOfWeek, MatchStatus status);
}
