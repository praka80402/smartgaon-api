package com.smartgaon.ai.smartgaon_api.dashboardstats;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
public class DashboardStatController {

    private final DashboardStatService service;

    @GetMapping
    public List<DashboardStat> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public DashboardStat getById(@PathVariable Long id) {
        return service.getById(id);
    }
}