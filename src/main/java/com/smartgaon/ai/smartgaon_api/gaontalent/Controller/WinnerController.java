package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.gaontalent.Service.WinnerService;

import lombok.RequiredArgsConstructor;

@RestController

@RequestMapping("/api/gaon-talent/winner")
@RequiredArgsConstructor
public class WinnerController {

    private final WinnerService service;

    @PostMapping("/{entryId}")
    public ResponseEntity<?> markWinner(@PathVariable Long entryId) {
        return ResponseEntity.ok(service.declareWinner(entryId));
    }

    @GetMapping
    public ResponseEntity<?> allWinners() {
        return ResponseEntity.ok(service.getAllWinners());
    }

    @GetMapping("/filter")
    public ResponseEntity<?> winnersByFilter(
            @RequestParam(required = false) Long competitionId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(service.getWinnersByFilter(competitionId, year, month));
    }
}
