//package org.example.statsservice.controller;
//
//
//import org.example.statsservice.entity.MatchStats;
//import org.example.statsservice.service.StatsService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/stats")
//public class StatsController {
//
//    @Autowired
//    private StatsService statsService;
//
//    @PostMapping
//    public MatchStats createStats(@RequestBody MatchStats stats) {
//        return statsService.save(stats);
//    }
//
//
//    @GetMapping("/match/{matchId}")
//    public List<MatchStats> getStatsByMatch(@PathVariable Long matchId) {
//        return statsService.getStatsForMatch(matchId);
//    }
//
//    @PostMapping("/fetch-from-api/{matchId}")
//    public String triggerManualFetch(@PathVariable Long matchId) {
//        // On appelle la méthode qui fait le travail (Feign + Save)
//        statsService.fetchAndSaveStats(matchId);
//        return "Commande envoyée : Récupération des stats pour le match " + matchId;
//    }
//}
package org.example.statsservice.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.example.statsservice.entity.MatchStats;
import org.example.statsservice.repository.StatsRepository;
import org.example.statsservice.service.StatsService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;
    private final StatsRepository statsRepository;


    @GetMapping("/match/{matchId}")
    public ResponseEntity<List<MatchStats>> getStatsByMatch(@PathVariable Long matchId) {
        List<MatchStats> stats = statsService.getStatsForMatch(matchId);
        if (stats.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/history")
    public ResponseEntity<List<MatchStats>> getHistory(@RequestParam String teamName, @RequestParam(defaultValue = "5") int limit){
        List<MatchStats> history = statsRepository.findLastMatchesByTeamName(teamName, PageRequest.of(0, limit));
        return ResponseEntity.ok(history);
    }


    @PostMapping("/manual-process")
    public ResponseEntity<String> manualProcess(@RequestBody ManualProcessRequest request) {
        statsService.processMatchEvent(
                request.getMatchId(),
                request.getHomeTeam(),
                request.getAwayTeam(),
                request.getDate()
        );
        return ResponseEntity.ok("Manual CSV processing triggered for: " + request.getHomeTeam() + " vs " + request.getAwayTeam());
    }

    @PostMapping("/bulk-import")
    public ResponseEntity<String> triggerBulkImport() {
        String result = statsService.bulkImportAllMatches();
        return ResponseEntity.ok(result);
    }

    @Data
    public static class ManualProcessRequest {
        private Long matchId;
        private String homeTeam;
        private String awayTeam;
        private String date;
    }
}