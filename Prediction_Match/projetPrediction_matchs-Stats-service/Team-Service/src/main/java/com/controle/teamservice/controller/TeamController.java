package com.controle.teamservice.controller;



import com.controle.teamservice.entity.Team;
import com.controle.teamservice.repository.TeamRepository;
import com.controle.teamservice.service.TeamService;
import com.controle.teamservice.service.TeamServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamServiceImpl teamService;
    private final TeamRepository teamRepository;


    @GetMapping
    public List<Team> getTeams2026() {

        return teamRepository.findAll();
    }
    @GetMapping("/{name}")
    public Team getTeamByName(@PathVariable String name) {
        return teamService.getTeamByName(name);
    }

    @GetMapping("/standings")
    public JsonNode getStandings(){
        return teamService.getStandings();
    }
}

