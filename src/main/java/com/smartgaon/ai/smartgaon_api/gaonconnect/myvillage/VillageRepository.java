package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface VillageRepository extends JpaRepository<Village, Long> {
    @Query("""
    SELECT v FROM Village v
    WHERE v.name LIKE :name
      AND v.city LIKE :city
      AND v.state LIKE :state
""")
    Page<Village> searchVillages(String name, String city, String state, Pageable pageable);

}

