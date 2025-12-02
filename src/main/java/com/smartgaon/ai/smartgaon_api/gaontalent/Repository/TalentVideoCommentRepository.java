package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

public interface TalentVideoCommentRepository extends JpaRepository<TalentVideoComment, Long> {

    Page<TalentVideoComment> findByVideoIdOrderByCreatedAtDesc(String videoId, Pageable pageable);
    void deleteByVideoId(String videoId);

}

