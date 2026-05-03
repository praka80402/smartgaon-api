package com.smartgaon.ai.smartgaon_api.dashboardstats;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardStatService {

    private final DashboardStatRepository repo;

    public List<DashboardStat> getAll() {
        return repo.findAll();
    }

    public DashboardStat getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Stat not found"));
    }
}