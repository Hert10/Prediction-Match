package com.example.demo.DTOS;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class PredictionResult {
    private String homeTeam;
    private String awayTeam;
    private Map<String, Double> probabilities;
    private String recommendation;
}
