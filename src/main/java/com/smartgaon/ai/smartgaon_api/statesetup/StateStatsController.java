package com.smartgaon.ai.smartgaon_api.statesetup;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
public class StateStatsController {

    private final StateStatsService service;

    @GetMapping
    public List<StateStats> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public StateStats getById(@PathVariable Long id) {
        return service.getById(id);
    }
    @GetMapping("/by-name")
    public StateStats getByName(@RequestParam String stateName) {
    return service.getByStateName(stateName);
}
}