package com.smartgaon.ai.smartgaon_api.location.service;

import com.smartgaon.ai.smartgaon_api.location.entity.*;
import com.smartgaon.ai.smartgaon_api.location.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final StateRepository stateRepo;
    private final DistrictRepository districtRepo;
    private final PincodeRepository pincodeRepo;

    public List<State> getAllStates() {
        return stateRepo.findAllByOrderByNameAsc();
    }

    public List<District> getDistrictsByState(Long stateId) {
        return districtRepo.findByStateIdOrderByNameAsc(stateId);
    }

    public List<Pincode> getPincodesByDistrict(Long districtId) {
        return pincodeRepo.findByDistrictIdOrderByPincodeAsc(districtId);
    }
}