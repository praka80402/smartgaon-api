package com.smartgaon.ai.smartgaon_api.latestvillage.sg;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VillageRequestRepository extends JpaRepository<VillageRequest, Long> {
    List<VillageRequest> findAllByStatusOrderByCreatedAtDesc(VillageRequest.Status status);
    List<VillageRequest> findAllByOrderByCreatedAtDesc();
    long countByStatus(VillageRequest.Status status);
}
