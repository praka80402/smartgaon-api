package com.smartgaon.ai.smartgaon_api.statesetup;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateStatsService {

    private final StateStatsRepository repo;

    public List<StateStats> getAll() {
        return repo.findAll();
    }

    public StateStats getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("State not found"));
    }
    public StateStats getByStateName(String stateName) {
    return repo.findByStateNameIgnoreCase(stateName)
            .orElseThrow(() -> new RuntimeException("State not found"));
}
}