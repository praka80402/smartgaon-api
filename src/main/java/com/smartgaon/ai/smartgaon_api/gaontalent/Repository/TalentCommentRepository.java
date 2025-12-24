package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentComment;

public interface TalentCommentRepository extends JpaRepository<TalentComment, Long> {

    List<TalentComment> findByEntryIdOrderByCreatedAtDesc(Long entryId);

    int countByEntryId(Long entryId);
}
