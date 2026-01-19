package com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPostReport;

public interface ForumPostReportRepository extends JpaRepository<ForumPostReport, Long> {

    boolean existsByPost_PostIdAndReportedByUserId(Long postId, Long userId);

    List<ForumPostReport> findByReportedByUserId(Long userId);
}
