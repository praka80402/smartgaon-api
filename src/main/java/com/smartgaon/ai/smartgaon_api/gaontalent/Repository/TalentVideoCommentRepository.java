package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TalentVideoCommentRepository extends JpaRepository<TalentVideoComment, Long> {

    List<TalentVideoComment> findByVideoIdOrderByCreatedAtDesc(String videoId);
}

