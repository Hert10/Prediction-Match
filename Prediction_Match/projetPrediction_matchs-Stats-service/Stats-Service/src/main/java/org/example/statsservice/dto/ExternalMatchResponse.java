package org.example.statsservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ExternalMatchResponse {

    // L'API renvoie une liste "response" contenant 2 éléments (Stats Équipe A, Stats Équipe B)
    private List<ResponseData> response;

    @Data
    public static class ResponseData {
        private Team team;
        private List<Stat> statistics;
    }

    @Data
    public static class Team {
        private Long id;
        private String name;
    }

    @Data
    public static class Stat {
        private String type;  // Ex: "Ball Possession", "Total Shots", "expected_goals"
        private Object value; // Attention: peut être un int (5) ou un String ("50%") ou null
    }
}