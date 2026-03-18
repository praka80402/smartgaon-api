package com.smartgaon.ai.smartgaon_api.gaonsathiimage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GaonSathiRepository extends JpaRepository<Gaon_Sathi_Image, Long> {
}
