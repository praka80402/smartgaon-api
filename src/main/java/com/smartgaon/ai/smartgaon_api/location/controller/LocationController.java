package com.smartgaon.ai.smartgaon_api.location.controller;

import com.smartgaon.ai.smartgaon_api.location.entity.*;
import com.smartgaon.ai.smartgaon_api.location.service.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping("/states")
    public List<State> getStates() {
        return locationService.getAllStates();
    }

    @GetMapping("/districts")
    public List<District> getDistricts(@RequestParam Long stateId) {
        return locationService.getDistrictsByState(stateId);
    }

    @GetMapping("/pincodes")
    public List<Pincode> getPincodes(@RequestParam Long districtId) {
        return locationService.getPincodesByDistrict(districtId);
    }
}