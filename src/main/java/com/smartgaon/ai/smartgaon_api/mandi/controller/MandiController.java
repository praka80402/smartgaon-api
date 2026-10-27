package com.smartgaon.ai.smartgaon_api.mandi.controller;

import com.smartgaon.ai.smartgaon_api.mandi.dto.MandiTrendDTO;
import com.smartgaon.ai.smartgaon_api.mandi.entity.MandiRate;
import com.smartgaon.ai.smartgaon_api.mandi.service.MandiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mandi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MandiController {

    private final MandiService service;

    /**
     * GET /api/mandi/rates
     * Parameters: state, district, search
     */
    @GetMapping("/rates")
    public ResponseEntity<List<MandiRate>> getRates(
            @RequestParam(required = false, defaultValue = "Bihar") String state,
            @RequestParam(required = false, defaultValue = "Patna") String district,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(service.getMandiRates(state, district, search));
    }

    /**
     * GET /api/mandi/trend
     * Parameters: state, district, commodity, days
     * Returns 30-day historical trend, price delta, and weekly bucket distribution for graphs.
     */
    @GetMapping("/trend")
    public ResponseEntity<MandiTrendDTO> getTrend(
            @RequestParam(required = false, defaultValue = "Bihar") String state,
            @RequestParam(required = false, defaultValue = "Patna") String district,
            @RequestParam(defaultValue = "Wheat") String commodity,
            @RequestParam(required = false, defaultValue = "30") Integer days
    ) {
        return ResponseEntity.ok(service.getMandiTrend(state, district, commodity, days));
    }

    /**
     * GET /api/mandi/states
     */
    @GetMapping("/states")
    public ResponseEntity<List<String>> getStates() {
        return ResponseEntity.ok(service.getAvailableStates());
    }

    /**
     * GET /api/mandi/districts
     */
    @GetMapping("/districts")
    public ResponseEntity<List<String>> getDistricts(
            @RequestParam(required = false, defaultValue = "Uttar Pradesh") String state
    ) {
        return ResponseEntity.ok(service.getDistrictsByState(state));
    }

    /**
     * POST /api/mandi/sync
     * Triggers sync from data.gov.in Agmarknet API
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncLiveRates(
            @RequestParam(required = false) String apiKey,
            @RequestParam(required = false, defaultValue = "50") Integer limit
    ) {
        return ResponseEntity.ok(service.syncGovtMandiData(apiKey, limit));
    }

    /**
     * POST /api/mandi/purge
     * Triggers 30-day retention cleanup manually or via administrative hook.
     */
    @PostMapping("/purge")
    public ResponseEntity<Map<String, Object>> purgeOldRates() {
        return ResponseEntity.ok(service.purgeOldMandiRates());
    }
}
