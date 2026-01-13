//package com.controle.teamservice.service;
//
//import com.controle.teamservice.entity.Team;
//import com.controle.teamservice.repository.TeamRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import jakarta.annotation.PostConstruct;
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class TeamFetcherService {
//
//    private final TeamRepository teamRepository;
//
//    private final WebClient webClient = WebClient.builder()
//            .baseUrl("https://v3.football.api-sports.io")
//            .defaultHeader("x-apisports-key", "44dd33be56c31cee35030d8f9566b16e")
//            .build();
//
//    @PostConstruct
//    @Transactional
//    public void fetchAndSaveTeams() {
//        // 1️⃣ Récupération du JSON depuis l'API
//        JsonNode json = webClient.get()
//                .uri("/teams?league=39&season=2025")
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .block(); // attend la réponse
//
//        System.out.println("JSON reçu : " + json); // <-- debug
//
//        if (json != null && json.has("response")) {
//            List<Team> teamsToSave = new ArrayList<>();
//
//            json.get("response").forEach(item -> {
//                JsonNode teamNode = item.get("team");
//                JsonNode venueNode = item.get("venue");
//
//                Team team = Team.builder()
//                        .name(teamNode.get("name").asText())
//                        .foundedYear(teamNode.get("founded").asInt())
//                        .stadium(venueNode.get("name").asText())
//                        .city(venueNode.get("city").asText())
//                        .capacity(venueNode.get("capacity").asInt())
//                        .logo(teamNode.get("logo").asText())
//                        .league("Premier League")
//                        .build();
//
//                // évite les doublons
//                if (!teamRepository.existsByName(team.getName())) {
//                    teamsToSave.add(team);
//                }
//            });
//
//            // Sauvegarde de toutes les équipes en une seule fois
//            teamRepository.saveAll(teamsToSave);
//
//            System.out.println(teamsToSave.size() + " équipes de Premier League ont été importées !");
//        } else {
//            System.out.println("Aucune équipe reçue de l'API !");
//        }
//    }
//}

package com.controle.teamservice.service;

import com.controle.teamservice.entity.Team;
import com.controle.teamservice.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamFetcherService {

    private final TeamRepository teamRepository;

    // Use the football-data.org configuration
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.football-data.org/v4")
            .defaultHeader("X-Auth-Token", "7ac6c5ec30824482a658a789347490c4") // Replace with your new key
            .build();

    @PostConstruct
    @Transactional
    public void fetchAndSaveTeams() {
        // 2021 is the ID for Premier League in football-data.org
        JsonNode json = webClient.get()
                .uri("/competitions/2021/teams?season=2025")
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        System.out.println("JSON reçu : " + json);

        // This API returns a "teams" array, not "response"
        if (json != null && json.has("teams")) {
            List<Team> teamsToSave = new ArrayList<>();

            json.get("teams").forEach(teamNode -> {
                // Mapping adjustments for football-data.org structure
                String name = teamNode.has("name") ? teamNode.get("name").asText() : "Unknown";
                int founded = teamNode.has("founded") ? teamNode.get("founded").asInt() : 0;
                String stadium = teamNode.has("venue") ? teamNode.get("venue").asText() : "Unknown";
                String logo = teamNode.has("crest") ? teamNode.get("crest").asText() : "";

                // This API provides 'address' instead of specific 'city'
                String address = teamNode.has("address") ? teamNode.get("address").asText() : "Unknown";

                Team team = Team.builder()
                        .name(name)
                        .foundedYear(founded)
                        .stadium(stadium)
                        .city(address)
                        .capacity(0)
                        .logo(logo)
                        .league("Premier League")
                        .build();

                // Avoid duplicates
                if (!teamRepository.existsByName(team.getName())) {
                    teamsToSave.add(team);
                }
            });

            teamRepository.saveAll(teamsToSave);
            System.out.println(teamsToSave.size() + " équipes de Premier League ont été importées !");
        } else {
            System.out.println("Aucune équipe reçue de l'API !");
        }
    }

    // New method to fetch standings
    public JsonNode getStandings() {
        try {
            JsonNode json = webClient.get()
                    .uri("/competitions/2021/standings?season=2025")
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            System.out.println("Classement récupéré avec succès.");
            return json;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du classement: " + e.getMessage());
            return null;
        }
    }
}