package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentLike;

import java.util.Optional;

public interface TalentLikeRepository extends JpaRepository<TalentLike, Long> {
    Optional<TalentLike> findByEntryIdAndUserId(Long entryId, Long userId);
    int countByEntryId(Long entryId);
}

