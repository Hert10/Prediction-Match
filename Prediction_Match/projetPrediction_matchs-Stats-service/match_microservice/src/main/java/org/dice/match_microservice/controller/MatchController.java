package org.dice.match_microservice.controller;


import lombok.AllArgsConstructor;
import org.dice.match_microservice.entity.Match;
import org.dice.match_microservice.entity.MatchStatus;
import org.dice.match_microservice.repository.matchRepository;
import org.dice.match_microservice.service.MatchDataService;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final matchRepository matchRepository;
    private final MatchDataService matchDataService;


    @GetMapping("/upcoming")
    public List<Match> fetchMatchesForThisWeek() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfWeek = now
                .with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                .with(LocalTime.MAX);

        return matchRepository.findByMatchDateBetweenAndStatus(
                startOfToday,
                endOfWeek,
                MatchStatus.SCHEDULED
        );
    }

    @GetMapping("/calendar")
    public List<Match> getcalendar(){
        return matchRepository.findAll();
    }


    @PostMapping("/sync")
    public String syncMatches() {
        matchDataService.fetchCalendar();
        return "Sync triggered successfully";
    }
    @GetMapping("/finished")
    public List<Match>fetchFinishedMatches(){
        return matchRepository.findByStatus(MatchStatus.FINISHED);
    }

    @PostMapping("/trigger-last")
    public String triggerLastFinishedMatch() {
        matchDataService.triggerLastFinishedMatch();
        return "Triggered event for the last finished match. Check console logs.";
    }
    @PostMapping("/trigger-first")
    public String triggerfirstleaguematch(){
        matchDataService.triggerfirstFInishedMatch();
        return "triggered event for first finished match";
    }
}