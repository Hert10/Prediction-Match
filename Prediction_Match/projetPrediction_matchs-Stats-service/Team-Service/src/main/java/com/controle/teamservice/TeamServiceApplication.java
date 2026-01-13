package com.controle.teamservice;

import com.controle.teamservice.service.TeamFetcherService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class TeamServiceApplication implements CommandLineRunner {

    private final TeamFetcherService teamFetcherService;

    public static void main(String[] args) {
        SpringApplication.run(TeamServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        teamFetcherService.fetchAndSaveTeams();
    }
}

