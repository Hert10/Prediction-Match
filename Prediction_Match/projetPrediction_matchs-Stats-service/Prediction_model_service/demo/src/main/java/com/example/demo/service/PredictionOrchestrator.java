package com.example.demo.service;


import com.example.demo.DTOS.MatchDTO;
import com.example.demo.DTOS.PredictionResult;
import com.example.demo.DTOS.StatsDTO;
import com.example.demo.clients.MatchClient;
import com.example.demo.clients.StatsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.Arrays;


@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionOrchestrator {

    private final MatchClient matchClient;
    private final StatsClient statsClient;
    private final EmbeddedPredictionService modelService;

    private final List<String> validCsvTeamNames = Arrays.asList(
            "Liverpool", "Bournemouth", "Aston Villa", "Newcastle", "Brighton",
            "Fulham", "Sunderland", "West Ham", "Tottenham", "Burnley",
            "Wolves", "Man City", "Chelsea", "Crystal Palace", "Nott'm Forest",
            "Brentford", "Man United", "Arsenal", "Leeds", "Everton"
    );

    public List<PredictionResult> predictUpcomingMatches() {
        log.info("Starting prediction cycle...");

        List<MatchDTO> upcomingMatches = matchClient.getUpcomingMatches();
        List<PredictionResult> results = new ArrayList<>();

        if (upcomingMatches == null || upcomingMatches.isEmpty()) {
            log.warn("No upcoming matches found.");
            return results;
        }

        for (MatchDTO match : upcomingMatches) {
            try {
                String homeTeamCsvName = findFuzzyMatchName(match.getHomeTeamName());
                String awayTeamCsvName = findFuzzyMatchName(match.getAwayTeamName());

                log.info("Predicting: {} ({}) vs {} ({})",
                        match.getHomeTeamName(), homeTeamCsvName,
                        match.getAwayTeamName(), awayTeamCsvName);

                List<StatsDTO> homeHistory = statsClient.getTeamHistory(homeTeamCsvName, 5);
                List<StatsDTO> awayHistory = statsClient.getTeamHistory(awayTeamCsvName, 5);

                double[] homeFeatures = calculateFeatures(homeHistory);
                double[] awayFeatures = calculateFeatures(awayHistory);


                Map<String, Double> prediction = modelService.predict(
                        homeFeatures[0], homeFeatures[1], homeFeatures[2],
                        awayFeatures[0], awayFeatures[1], awayFeatures[2]
                );

                results.add(new PredictionResult(
                        match.getHomeTeamName(),
                        match.getAwayTeamName(),
                        prediction,
                        determineRecommendation(prediction)
                ));

            } catch (Exception e) {
                log.error("Failed to predict match {} vs {}: {}",
                        match.getHomeTeamName(), match.getAwayTeamName(), e.getMessage());
            }
        }

        return results;
    }


    private String findFuzzyMatchName(String apiName) {
        if (apiName == null) return "";

        if (apiName.contains("Manchester United")) return "Man United";
        if (apiName.contains("Manchester City")) return "Man City";
        if (apiName.contains("Nottingham")) return "Nott'm Forest";
        if (apiName.contains("Wolverhampton")) return "Wolves";
        if (apiName.contains("Sheffield")) return "Sheff Utd";


        for (String csvName : validCsvTeamNames) {
            if (isFuzzyMatch(csvName, apiName)) {
                return csvName;
            }
        }

        return apiName;
    }

    private boolean isFuzzyMatch(String dbName, String csvName) {
        String n1 = normalize(dbName);
        String n2 = normalize(csvName);
        return n1.contains(n2) || n2.contains(n1);
    }

    private String normalize(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replace("fc", "")
                .replace("afc", "")
                .replaceAll("\\s+", "")
                .trim();
    }

    private double[] calculateFeatures(List<StatsDTO> history) {
        if (history == null || history.isEmpty()) {
            return new double[]{0.0, 0.0, 0.0};
        }

        double totalPoints = 0;
        double totalScored = 0;
        double totalConceded = 0;

        for (StatsDTO stat : history) {
            totalScored += stat.getGoalsScored();
            totalConceded += stat.getGoalsConceded();

            if (stat.getGoalsScored() > stat.getGoalsConceded()) {
                totalPoints += 3;
            } else if (stat.getGoalsScored() == stat.getGoalsConceded()) {
                totalPoints += 1;
            }
        }

        int count = history.size();
        return new double[] {
                totalPoints / count,
                totalScored / count,
                totalConceded / count
        };
    }


    private String determineRecommendation(Map<String, Double> probs) {
        return probs.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Draw");
    }
}