package com.example.demo.DTOS;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MatchDTO {
    private String homeTeamName;
    private String awayTeamName;
    private LocalDateTime matchDate;
}
