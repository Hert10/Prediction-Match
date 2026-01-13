//package org.example.statsservice.service;
//
//import org.example.statsservice.client.FootballApiClient;
//import org.example.statsservice.dto.ExternalMatchResponse;
//import org.example.statsservice.entity.MatchStats;
//import org.example.statsservice.repository.StatsRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class StatsService {
//
//    @Autowired
//    private StatsRepository statsRepository;
//
//    @Autowired
//    private FootballApiClient footballApiClient;
//
//    @Value("${football.api.key}")
//    private String apiKey;
//
//    // --- MÉTHODE 1 : Sauvegarde Manuelle (via Controller) ---
//    public MatchStats saveStats(MatchStats stats) {
//        List<MatchStats> existing = statsRepository.findByMatchId(stats.getMatchId());
//        if (!existing.isEmpty()) {
//            System.out.println("Stats déjà existantes pour le match " + stats.getMatchId());
//            return existing.get(0);
//        }
//        return statsRepository.save(stats);
//    }
//
//    // --- MÉTHODE 2 : Automatique (via Kafka ou Test Postman) ---
//    public void fetchAndSaveStats(Long matchId) {
//        // 1. Vérifier si on a déjà les stats
//        if (!statsRepository.findByMatchId(matchId).isEmpty()) {
//            System.out.println("Stats déjà présentes pour le match " + matchId + ". Skip.");
//            return;
//        }
//
//        // 2. Appeler l'API Externe (Attention à l'ordre des paramètres selon votre Interface Client)
//        // Si votre interface est getStats(apiKey, matchId), gardez cet ordre.
//        ExternalMatchResponse response = footballApiClient.getStats(matchId,apiKey);
//
//        if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
//            System.out.println("Aucune donnée reçue de l'API pour le match " + matchId);
//            return;
//        }
//
//        // 3. Boucler sur les résultats (Domicile et Extérieur)
//        for (ExternalMatchResponse.ResponseData data : response.getResponse()) {
//
//            MatchStats statsEntity = new MatchStats();
//            statsEntity.setMatchId(matchId);
//
//            // On récupère l'ID de l'équipe (si disponible dans le DTO Team)
//            if (data.getTeam() != null) {
//                statsEntity.setTeamId(data.getTeam().getId());
//            }
//
//            // --- LE MAPPING (C'est ici qu'on remplit les 0) ---
//            if (data.getStatistics() != null) {
//                for (ExternalMatchResponse.Stat stat : data.getStatistics()) {
//                    if (stat.getValue() == null) continue;
//
//                    String type = stat.getType();
//                    String valueStr = stat.getValue().toString();
//
//                    switch (type) {
//                        case "Ball Possession":
//                            statsEntity.setPossession(parsePercentage(valueStr));
//                            break;
//                        case "Total Shots":
//                            statsEntity.setShots(parseInteger(valueStr));
//                            break;
//                        case "expected_goals":
//                            statsEntity.setExpectedGoals(parseDouble(valueStr));
//                            break;
//                        case "Goals": // Parfois utile si l'API le renvoie ici
//                            statsEntity.setGoalsScored(parseInteger(valueStr));
//                            break;
//                        // Ajoutez ici d'autres cas (Corners, Fouls, etc.)
//                    }
//                }
//            }
//
//            // 4. Sauvegarder dans la BDD
//            statsRepository.save(statsEntity);
//            System.out.println("Stats sauvegardées pour l'équipe ID: " + statsEntity.getTeamId());
//        }
//    }
//
//    public List<MatchStats> getStatsForMatch(Long matchId) {
//        return statsRepository.findByMatchId(matchId);
//    }
//
//    // --- Méthodes utilitaires pour nettoyer les données ---
//
//    private double parsePercentage(String value) {
//        try {
//            if (value == null) return 0.0;
//            return Double.parseDouble(value.replace("%", "").trim());
//        } catch (NumberFormatException e) {
//            return 0.0;
//        }
//    }
//
//    private int parseInteger(String value) {
//        try {
//            if (value == null) return 0;
//            return Integer.parseInt(value);
//        } catch (NumberFormatException e) {
//            return 0;
//        }
//    }
//
//    private double parseDouble(String value) {
//        try {
//            if (value == null) return 0.0;
//            return Double.parseDouble(value);
//        } catch (NumberFormatException e) {
//            return 0.0;
//        }
//    }
//}

//package org.example.statsservice.service;
//
//import lombok.Data;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVParser;
//import org.apache.commons.csv.CSVRecord;
//import org.example.statsservice.entity.MatchStats;
//import org.example.statsservice.repository.StatsRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.Reader;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.StandardCopyOption;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//
//@Service
//public class StatsService {
//
//    @Autowired
//    private StatsRepository statsRepository;
//
//    // Direct link to the CSV (2023/2024 Season)
//    private static final String CSV_URL = "https://www.football-data.co.uk/mmz4281/2526/E0.csv";
//
//    public void processMatchEvent(Long matchId, String homeTeam, String awayTeam, String dateStr) {
//        Path tempCsvPath = null;
//        try {
//            System.out.println("Processing stats for: " + homeTeam + " vs " + awayTeam);
//
//            // 1. Download CSV to a temporary file
//            tempCsvPath = Files.createTempFile("match_data_", ".csv");
//            try (InputStream in = new URL(CSV_URL).openStream()) {
//                Files.copy(in, tempCsvPath, StandardCopyOption.REPLACE_EXISTING);
//            }
//
//            // 2. Parse CSV
//            try (Reader reader = new InputStreamReader(Files.newInputStream(tempCsvPath));
//                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
//
//                boolean found = false;
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//                LocalDate targetDate = LocalDate.parse(dateStr); // Assumes YYYY-MM-DD from event
//
//                for (CSVRecord record : csvParser) {
//                    String csvDateStr = record.get("Date");
//                    String csvHome = record.get("HomeTeam");
//                    String csvAway = record.get("AwayTeam");
//
//                    // 3. Match Logic (Fuzzy Name + Exact Date)
//                    LocalDate csvDate = LocalDate.parse(csvDateStr, formatter);
//
//                    // Allow small date discrepancy (timezone diffs) or exact match
//                    boolean dateMatch = csvDate.isEqual(targetDate);
//                    boolean nameMatch = isFuzzyMatch(homeTeam, csvHome) && isFuzzyMatch(awayTeam, csvAway);
//
//                    if (dateMatch && nameMatch) {
//                        saveStatsFromRecord(matchId, record, true); // Save Home Stats
//                        saveStatsFromRecord(matchId, record, false); // Save Away Stats
//                        found = true;
//                        System.out.println("✅ Stats found and saved.");
//                        break;
//                    }
//                }
//
//                if (!found) {
//                    System.err.println("❌ Match not found in CSV: " + homeTeam + " vs " + awayTeam);
//                }
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            // 4. Delete CSV
//            if (tempCsvPath != null) {
//                try {
//                    Files.deleteIfExists(tempCsvPath);
//                    System.out.println("🗑️ CSV file deleted.");
//                } catch (Exception e) {
//                    System.err.println("Failed to delete temp CSV.");
//                }
//            }
//        }
//    }
//
//    private void saveStatsFromRecord(Long matchId, CSVRecord record, boolean isHome) {
//        MatchStats stats = new MatchStats();
//        stats.setMatchId(matchId);
//        stats.setHome(isHome);
//        stats.setReferee(record.get("Referee")); // Common to both
//
//        if (isHome) {
//            stats.setTeamName(record.get("HomeTeam"));
//
//            // Goals
//            stats.setGoalsScored(parseInt(record.get("FTHG")));
//            stats.setGoalsConceded(parseInt(record.get("FTAG")));
//            stats.setHalfTimeGoals(parseInt(record.get("HTHG")));
//
//            // Stats
//            stats.setShots(parseInt(record.get("HS")));
//            stats.setShotsOnTarget(parseInt(record.get("HST")));
//            stats.setCorners(parseInt(record.get("HC")));
//            stats.setFouls(parseInt(record.get("HF")));
//
//            // Cards
//            stats.setYellowCards(parseInt(record.get("HY")));
//            stats.setRedCards(parseInt(record.get("HR")));
//
//        } else {
//            stats.setTeamName(record.get("AwayTeam"));
//
//            // Goals (Swapped for Away team)
//            stats.setGoalsScored(parseInt(record.get("FTAG")));
//            stats.setGoalsConceded(parseInt(record.get("FTHG")));
//            stats.setHalfTimeGoals(parseInt(record.get("HTAG")));
//
//            // Stats
//            stats.setShots(parseInt(record.get("AS")));
//            stats.setShotsOnTarget(parseInt(record.get("AST")));
//            stats.setCorners(parseInt(record.get("AC")));
//            stats.setFouls(parseInt(record.get("AF")));
//
//            // Cards
//            stats.setYellowCards(parseInt(record.get("AY")));
//            stats.setRedCards(parseInt(record.get("AR")));
//        }
//
//        statsRepository.save(stats);
//    }
//
//    private boolean isFuzzyMatch(String dbName, String csvName) {
//        String n1 = normalize(dbName);
//        String n2 = normalize(csvName);
//        return n1.contains(n2) || n2.contains(n1);
//    }
//
//    private String normalize(String name) {
//        if (name == null) return "";
//        return name.toLowerCase()
//                .replace("fc", "")
//                .replace("afc", "")
//                .replaceAll("\\s+", "")
//                .trim();
//    }
//
//    public List<MatchStats> getStatsForMatch(Long matchId) {
//        return statsRepository.findByMatchId(matchId);
//    }
//
//    private int parseInt(String val) {
//        try { return Integer.parseInt(val); } catch (Exception e) { return 0; }
//    }
//}

package org.example.statsservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.statsservice.entity.MatchStats;
import org.example.statsservice.entity.PendingMatch;
import org.example.statsservice.repository.PendingMatchRepository;
import org.example.statsservice.repository.StatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class StatsService {

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private PendingMatchRepository pendingMatchRepository;

    private static final String CSV_URL = "https://www.football-data.co.uk/mmz4281/2526/E0.csv";
    private static final DateTimeFormatter CSV_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void processMatchEvent(Long matchId, String homeTeam, String awayTeam, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);

        if (!statsRepository.findByMatchId(matchId).isEmpty()) {
            log.info("Stats already exist for match {}", matchId);
            return;
        }

        boolean found = tryFindAndSave(matchId, homeTeam, awayTeam, date, null);

        if (!found) {
            log.info("Match stats not found yet. Adding to pending queue: {} vs {}", homeTeam, awayTeam);
            addToPending(matchId, homeTeam, awayTeam, date);
        }
    }

    public List<MatchStats> getStatsForMatch(Long matchId) {
        return statsRepository.findByMatchId(matchId);
    }

    @Scheduled(cron = "0 0 */6 * * *")
    public void processPendingMatches() {
        List<PendingMatch> pendingMatches = pendingMatchRepository.findAll();
        if (pendingMatches.isEmpty()) return;

        log.info("Checking CSV for {} pending matches...", pendingMatches.size());

        List<CSVRecord> records = downloadCsvRecords();
        if (records.isEmpty()) return;

        List<PendingMatch> toDelete = new ArrayList<>();

        for (PendingMatch pm : pendingMatches) {
            boolean found = tryFindAndSave(pm.getMatchId(), pm.getHomeTeam(), pm.getAwayTeam(), pm.getMatchDate(), records);

            if (found) {
                toDelete.add(pm);
            } else {
                pm.setRetryCount(pm.getRetryCount() + 1);
                pendingMatchRepository.save(pm);
            }
        }

        if (!toDelete.isEmpty()) {
            pendingMatchRepository.deleteAll(toDelete);
            log.info("Successfully processed and removed {} pending matches.", toDelete.size());
        }
    }

    public String bulkImportAllMatches() {
        int savedCount = 0;
        int skippedCount = 0;


        long currentMatchId = 1L;

        log.info("Starting bulk import of all stats from CSV...");
        List<CSVRecord> records = downloadCsvRecords();

        if (records.isEmpty()) {
            return "Failed to download CSV or CSV is empty.";
        }

        for (CSVRecord record : records) {
            try {
                String dateStr = record.get("Date");
                LocalDate matchDate = LocalDate.parse(dateStr, CSV_DATE_FMT);
                String homeTeam = record.get("HomeTeam");

                if (statsRepository.existsByTeamNameAndMatchDate(homeTeam, matchDate)) {
                    skippedCount++;

                    currentMatchId++;
                    continue;
                }

                saveBulkRecord(record, true, matchDate, currentMatchId);
                saveBulkRecord(record, false, matchDate, currentMatchId);

                savedCount++;
                currentMatchId++;

            } catch (Exception e) {
                log.error("Error processing CSV row: {}", e.getMessage());
            }
        }

        return String.format("Bulk Import Complete. Saved: %d matches. Skipped: %d.", savedCount, skippedCount);
    }


    private void addToPending(Long matchId, String home, String away, LocalDate date) {
        if (!pendingMatchRepository.existsByMatchId(matchId)) {
            PendingMatch pm = new PendingMatch();
            pm.setMatchId(matchId);
            pm.setHomeTeam(home);
            pm.setAwayTeam(away);
            pm.setMatchDate(date);
            pendingMatchRepository.save(pm);
        }
    }

    private boolean tryFindAndSave(Long matchId, String home, String away, LocalDate targetDate, List<CSVRecord> preLoadedRecords) {
        List<CSVRecord> records = (preLoadedRecords != null) ? preLoadedRecords : downloadCsvRecords();

        for (CSVRecord record : records) {
            try {
                String csvDateStr = record.get("Date");
                LocalDate csvDate = LocalDate.parse(csvDateStr, CSV_DATE_FMT);

                boolean dateMatch = csvDate.isEqual(targetDate);
                boolean nameMatch = isFuzzyMatch(home, record.get("HomeTeam"))
                        && isFuzzyMatch(away, record.get("AwayTeam"));

                if (dateMatch && nameMatch) {
                    saveStatsFromRecord(matchId, record, true);
                    saveStatsFromRecord(matchId, record, false);
                    log.info("✅ Stats found and saved for match {}", matchId);
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    private List<CSVRecord> downloadCsvRecords() {
        Path tempCsvPath = null;
        try {
            tempCsvPath = Files.createTempFile("match_data_", ".csv");
            try (InputStream in = new URL(CSV_URL).openStream()) {
                Files.copy(in, tempCsvPath, StandardCopyOption.REPLACE_EXISTING);
            }
            try (Reader reader = new InputStreamReader(Files.newInputStream(tempCsvPath));
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                return csvParser.getRecords();
            }
        } catch (Exception e) {
            log.error("Failed to download or parse CSV", e);
            return new ArrayList<>();
        } finally {
            if (tempCsvPath != null) {
                try { Files.deleteIfExists(tempCsvPath); } catch (Exception ignored) {}
            }
        }
    }

    private void saveStatsFromRecord(Long matchId, CSVRecord record, boolean isHome) {
        saveBulkRecord(record, isHome, null, matchId); // Reuse the bulk method
    }

    private void saveBulkRecord(CSVRecord record, boolean isHome, LocalDate date, Long matchId) {
        MatchStats stats = new MatchStats();
        stats.setMatchId(matchId);
        stats.setMatchDate(date);
        stats.setReferee(record.get("Referee"));
        stats.setHome(isHome);

        if (isHome) {
            stats.setTeamName(record.get("HomeTeam"));
            stats.setGoalsScored(parseInt(record.get("FTHG")));
            stats.setGoalsConceded(parseInt(record.get("FTAG")));
            stats.setHalfTimeGoals(parseInt(record.get("HTHG")));
            stats.setShots(parseInt(record.get("HS")));
            stats.setShotsOnTarget(parseInt(record.get("HST")));
            stats.setCorners(parseInt(record.get("HC")));
            stats.setFouls(parseInt(record.get("HF")));
            stats.setYellowCards(parseInt(record.get("HY")));
            stats.setRedCards(parseInt(record.get("HR")));
        } else {
            stats.setTeamName(record.get("AwayTeam"));
            stats.setGoalsScored(parseInt(record.get("FTAG")));
            stats.setGoalsConceded(parseInt(record.get("FTHG")));
            stats.setHalfTimeGoals(parseInt(record.get("HTAG")));
            stats.setShots(parseInt(record.get("AS")));
            stats.setShotsOnTarget(parseInt(record.get("AST")));
            stats.setCorners(parseInt(record.get("AC")));
            stats.setFouls(parseInt(record.get("AF")));
            stats.setYellowCards(parseInt(record.get("AY")));
            stats.setRedCards(parseInt(record.get("AR")));
        }

        statsRepository.save(stats);
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

    private int parseInt(String val) {
        try { return Integer.parseInt(val); } catch (Exception e) { return 0; }
    }
}