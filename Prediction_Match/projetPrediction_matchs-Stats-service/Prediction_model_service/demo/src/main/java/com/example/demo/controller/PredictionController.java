package com.example.demo.controller;


import com.example.demo.DTOS.PredictionResult;
import com.example.demo.service.PredictionOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionOrchestrator predictionOrchestrator;


    @GetMapping("/upcoming")
    public ResponseEntity<List<PredictionResult>> getUpcomingPredictions() {
        List<PredictionResult> results = predictionOrchestrator.predictUpcomingMatches();

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(results);
    }
}