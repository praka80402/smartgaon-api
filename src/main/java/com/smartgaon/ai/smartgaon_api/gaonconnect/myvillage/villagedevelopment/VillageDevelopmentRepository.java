package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;

import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface VillageDevelopmentRepository
        extends JpaRepository<VillageDevelopment, Long> {

    List<VillageDevelopment> findByVillageId(Long villageId);
}