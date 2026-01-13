package org.dice.match_microservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "matches")
@Data
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String matchId;

    @Column(unique = true)
    private Long apiFootballFixtureId;

    private String league;
    private String season;

    private Long homeTeamId;
    private Long awayTeamId;

    private String homeTeamName;
    private String awayTeamName;

    private LocalDateTime matchDate;

    private String stadium;

    @Enumerated(EnumType.STRING)
    private MatchStatus status;

    private Integer homeScore;
    private Integer awayScore;
}


