package org.example.statsservice.repository;


import org.example.statsservice.entity.MatchStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StatsRepository extends JpaRepository<MatchStats,Long> {
    List<MatchStats> findByMatchId(Long matchId);
    boolean existsByTeamNameAndMatchDate(String teamName, LocalDate matchDate);

    @Query("select s from MatchStats s where s.teamName = :teamName order by s.matchDate desc ")
    List<MatchStats> findLastMatchesByTeamName(@Param("teamName") String teamName, Pageable pageable);
}
