package org.dice.match_microservice.config;

import jakarta.annotation.PostConstruct;
import org.dice.match_microservice.service.MatchDataService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private final MatchDataService matchDataService;

    public SchedulerConfig(MatchDataService matchDataService) {
        this.matchDataService = matchDataService;
    }

    @PostConstruct
    public void onStartup() {
        matchDataService.fetchCalendar();
    }

    @Scheduled(cron = "0 0 8 * * MON")
    public void syncWeeklySchedule() {
        System.out.println("Weekly Schedule Sync: Checking for fixture updates...");
        matchDataService.fetchCalendar();
    }

    @Scheduled(cron = "0 */30 12-23 * * *")
    public void syncLiveScores() {
        System.out.println("Live Score Sync: Updating match statuses and scores...");
        matchDataService.fetchCalendar();
    }
}