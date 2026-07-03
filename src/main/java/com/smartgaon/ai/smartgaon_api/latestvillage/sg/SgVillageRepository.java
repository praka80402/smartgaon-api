package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SgVillageRepository extends JpaRepository<SgVillage, Long> {
    List<SgVillage> findBySmartGaonTrue();
}
