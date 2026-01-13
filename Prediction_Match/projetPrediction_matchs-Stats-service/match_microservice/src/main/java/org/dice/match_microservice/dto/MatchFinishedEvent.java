package org.dice.match_microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchFinishedEvent {
    private Long matchId;
    private String homeTeam;
    private String awayTeam;
    private String matchDate; // Format YYYY-MM-DD
}
