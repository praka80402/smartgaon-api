package com.smartgaon.ai.smartgaon_api.gaontalent.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        return ResponseEntity.ok(service.getWinners());
    }
}

