//package org.example.statsservice.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.*;
//
//@Entity
//@AllArgsConstructor
//@NoArgsConstructor
//@Data
//
//public class MatchStats {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private Long matchId;
//    private Long teamId;
//
//    private int goalsScored;
//    private int goalsConceded;
//    private int shots;
//    private double possession;
//    private double expectedGoals;
//}

package org.example.statsservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate matchDate;
    private Long matchId;


    // ID from your Match Service
    private String teamName;
    private boolean isHome;

    // --- Scoring ---
    private int goalsScored;
    private int goalsConceded;
    private int halfTimeGoals;

    // --- Attack ---
    private int shots;
    private int shotsOnTarget;
    private int corners;

    // --- Defense & Discipline ---
    private int fouls;
    private int yellowCards;
    private int redCards;

    // --- Metadata ---
    private String referee;
}
