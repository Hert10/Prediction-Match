package com.controle.teamservice.service;
import com.controle.teamservice.entity.Team;
import com.controle.teamservice.repository.TeamRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamFetcherService teamFetcherService;



    @Override
    public Team getTeamByName(String name) {
        return teamRepository.findByName(name);

    }

    public JsonNode getStandings(){
        return teamFetcherService.getStandings();
    }


}
