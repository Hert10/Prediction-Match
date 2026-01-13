package com.example.demo.clients;


import com.example.demo.DTOS.MatchDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "match-service", url = "${services.match.url}")
public interface MatchClient {

    @GetMapping("/api/matches/upcoming")
    List<MatchDTO> getUpcomingMatches();
}
