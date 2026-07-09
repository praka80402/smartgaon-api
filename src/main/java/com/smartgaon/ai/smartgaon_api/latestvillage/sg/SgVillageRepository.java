package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SgVillageRepository extends JpaRepository<SgVillage, Long> {
    List<SgVillage> findBySmartGaonTrue();

    @Query("SELECT v FROM SgVillage v WHERE (:pincode IS NOT NULL AND :pincode != '' AND v.pincode = :pincode) OR (LOWER(TRIM(v.name)) = LOWER(TRIM(:name)) AND LOWER(TRIM(v.district)) = LOWER(TRIM(:district)) AND LOWER(TRIM(v.state)) = LOWER(TRIM(:state)))")
    List<SgVillage> findMatchingVillage(@Param("name") String name, @Param("district") String district, @Param("state") String state, @Param("pincode") String pincode);
}
