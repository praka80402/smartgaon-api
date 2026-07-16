package com.smartgaon.ai.smartgaon_api.shikshaquiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.UploadHistory;
import java.util.List;

@Repository
public interface UploadHistoryRepository extends JpaRepository<UploadHistory, Long> {
    List<UploadHistory> findAllByOrderByCreatedAtDesc();
}
