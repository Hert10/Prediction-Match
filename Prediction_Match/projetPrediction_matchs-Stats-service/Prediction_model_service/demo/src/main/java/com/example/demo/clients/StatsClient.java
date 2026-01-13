package com.example.demo.clients;


import com.example.demo.DTOS.StatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "stats-service", url = "${services.stats.url}")
public interface StatsClient {

    @GetMapping("/api/stats/history")
    List<StatsDTO> getTeamHistory(@RequestParam("teamName") String teamName,
                                  @RequestParam("limit") int limit);
}