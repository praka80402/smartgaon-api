package com.smartgaon.ai.smartgaon_api.gaontalent.Repository;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentVideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TalentVideoLikeRepository extends JpaRepository<TalentVideoLike, Long> {

    Optional<TalentVideoLike> findByUserIdAndVideoId(Long userId, String videoId);

    int countByVideoId(String videoId);
}

