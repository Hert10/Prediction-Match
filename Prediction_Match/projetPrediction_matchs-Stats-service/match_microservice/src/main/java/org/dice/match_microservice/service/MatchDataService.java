package org.dice.match_microservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.dice.match_microservice.entity.Match;
import org.dice.match_microservice.entity.MatchStatus;
import org.dice.match_microservice.messaging.MatchEventProducer;
import org.dice.match_microservice.repository.matchRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MatchDataService {

    private final matchRepository matchRepository;
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MatchEventProducer matchEventProducer;

    @Value("${football.data.api.token}")
    private String apiToken;

    @Value("${football.data.api.url}")
    private String apiUrl;

    public void fetchCalendar() {
        try {
            System.out.println("Fetching full season schedule from Football-Data.org...");

            String jsonResponse = restClient.get()
                    .uri(apiUrl)
                    .header("X-Auth-Token", apiToken)
                    .retrieve()
                    .body(String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                System.err.println("Failed to fetch data from Football-Data.org");
                return;
            }

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode matchesNode = root.path("matches");

            if (matchesNode.isMissingNode()) {
                System.err.println("No 'matches' node found in API response.");
                return;
            }

            List<Match> matchesToSave = new ArrayList<>();
            List<Match> finishedMatches = new ArrayList<>();

            for (JsonNode matchNode : matchesNode) {
                // Use the ID from football-data.org
                String apiMatchId = String.valueOf(matchNode.get("id").asLong());

                Match match = matchRepository.findByMatchId(apiMatchId)
                        .orElse(new Match());

                match.setMatchId(apiMatchId);
                match.setSeason("2025/2026");
                match.setLeague("Premier League");

                if (matchNode.has("venue") && !matchNode.get("venue").isNull()) {
                    match.setStadium(matchNode.get("venue").asText());
                }

                JsonNode homeTeam = matchNode.get("homeTeam");
                JsonNode awayTeam = matchNode.get("awayTeam");

                match.setHomeTeamName(homeTeam.get("name").asText());
                match.setHomeTeamId(homeTeam.get("id").asLong());

                match.setAwayTeamName(awayTeam.get("name").asText());
                match.setAwayTeamId(awayTeam.get("id").asLong());

                String dateStr = matchNode.get("utcDate").asText();
                Instant instant = Instant.parse(dateStr);
                LocalDateTime matchDate = LocalDateTime.ofInstant(instant, ZoneId.of("UTC")).with(LocalTime.MIN);
                match.setMatchDate(matchDate);

                MatchStatus oldStatus = match.getStatus();
                String status = matchNode.get("status").asText();

                if ("FINISHED".equalsIgnoreCase(status)) {
                    match.setStatus(MatchStatus.FINISHED);
                    JsonNode score = matchNode.get("score").get("fullTime");
                    match.setHomeScore(score.path("home").asInt(0));
                    match.setAwayScore(score.path("away").asInt(0));

                    // Check if this is a NEWLY finished match to trigger the event
                    if (oldStatus == MatchStatus.SCHEDULED) {
                        finishedMatches.add(match);
                    }

                } else {
                    match.setStatus(MatchStatus.SCHEDULED);
                }

                matchesToSave.add(match);
            }

            matchRepository.saveAll(matchesToSave);

            // Send events for matches that just finished so Stats Service can download the CSV
            for (Match finishedMatch : finishedMatches) {
                System.out.println("Match " + finishedMatch.getMatchId() + " finished! Triggering Stats CSV download.");
                // Ensure your MatchEventProducer is updated to accept the Match object
                matchEventProducer.sendMatchFinishedEvent(finishedMatch);
            }

            System.out.println("Successfully synced " + matchesToSave.size() + " matches.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void triggerLastFinishedMatch() {
        Optional<Match> lastMatchOpt = matchRepository.findTopByStatusOrderByMatchDateDesc(MatchStatus.FINISHED);

        if (lastMatchOpt.isPresent()) {
            Match lastMatch = lastMatchOpt.get();
            System.out.println("Manual Trigger: Re-sending event for last finished match: "
                    + lastMatch.getHomeTeamName() + " vs " + lastMatch.getAwayTeamName());

            matchEventProducer.sendMatchFinishedEvent(lastMatch);
        } else {
            System.err.println("Manual Trigger Failed: No FINISHED matches found in the database.");
        }
    }

    public void triggerfirstFInishedMatch() {
        Optional<Match> lastMatchOpt = matchRepository.findTopByStatusOrderByMatchDateAsc(MatchStatus.FINISHED);

        if (lastMatchOpt.isPresent()) {
            Match lastMatch = lastMatchOpt.get();
            System.out.println("Manual Trigger: Re-sending event for first finished match: "
                    + lastMatch.getHomeTeamName() + " vs " + lastMatch.getAwayTeamName());

            matchEventProducer.sendMatchFinishedEvent(lastMatch);
        } else {
            System.err.println("Manual Trigger Failed: No FINISHED matches found in the database.");
        }
    }
}