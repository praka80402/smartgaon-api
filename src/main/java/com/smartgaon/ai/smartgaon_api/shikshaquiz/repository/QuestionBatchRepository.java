package com.smartgaon.ai.smartgaon_api.shikshaquiz.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuestionBatch;

@Repository
public interface QuestionBatchRepository extends JpaRepository<QuestionBatch, Long> {

    Optional<QuestionBatch> findFirstBySegmentKeyAndStatus(String segmentKey, QuestionBatch.Status status);

    // the batch that should be active now = latest scheduled batch whose slot time has passed
    Optional<QuestionBatch> findFirstBySegmentKeyAndScheduledSlotTimeLessThanEqualOrderByScheduledSlotTimeDesc(
            String segmentKey, LocalDateTime now);

    long countBySegmentKeyAndStatus(String segmentKey, QuestionBatch.Status status);

    List<QuestionBatch> findBySegmentKeyAndScheduledSlotTimeAfter(String segmentKey, LocalDateTime after);

    List<QuestionBatch> findBySegmentKeyOrderByScheduledSlotTimeAsc(String segmentKey);

    Optional<QuestionBatch> findFirstBySegmentKeyOrderByScheduledSlotTimeDesc(String segmentKey);

    @Query("SELECT DISTINCT b.segmentKey FROM QuestionBatch b")
    List<String> findAllSegmentKeys();

    List<QuestionBatch> findByStatusAndScheduledSlotTimeLessThanEqual(
            QuestionBatch.Status status, LocalDateTime now);
}
