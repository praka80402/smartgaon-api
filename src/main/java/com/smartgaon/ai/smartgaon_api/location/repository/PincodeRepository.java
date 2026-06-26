package com.smartgaon.ai.smartgaon_api.location.repository;

import com.smartgaon.ai.smartgaon_api.location.entity.Pincode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PincodeRepository extends JpaRepository<Pincode, Long> {
    List<Pincode> findByDistrictIdOrderByPincodeAsc(Long districtId);
}