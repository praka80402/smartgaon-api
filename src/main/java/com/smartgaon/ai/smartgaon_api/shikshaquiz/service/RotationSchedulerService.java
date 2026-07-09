package com.smartgaon.ai.smartgaon_api.shikshaquiz.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuestionBatch;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuestionBatchRepository;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.util.QuizTimeUtil;

/**
 * Rotation engine (Section 4.4): 6 fixed slots/day anchored at 5 AM.
 *
 * Runs on the ADMIN backend only (Section 2.1 write-ownership). Web/App
 * backends need no sync call — they read the same Redis key / DB rows.
 *
 * NOTE: requires @EnableScheduling on the Spring Boot application class.
 */
@Service
public class RotationSchedulerService {

    public static final String ACTIVE_BATCH_KEY_PREFIX = "quiz:active_batch:";

    @Autowired
    private QuestionBatchRepository batchRepo;

    @Autowired
    private QuizRedisService redis;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules(); // JavaTimeModule for LocalDateTime

    // NOTE: The @Scheduled rotation cron lives ONLY in the admin backend.
    // The website never runs the cron (its main class has no @EnableScheduling);
    // it just reads the currently-active batch via getActiveBatch(...) below.

    /**
     * Activate the batch scheduled for the current slot, expire the previous
     * ACTIVE one, and push the new batch into Redis with TTL until next slot.
     * Also callable manually from the admin panel (e.g. after a missed run).
     */
    public QuestionBatch activateNextBatch(String segmentKey) {
        LocalDateTime now = LocalDateTime.now();

        // expire whatever is currently active
        batchRepo.findFirstBySegmentKeyAndStatus(segmentKey, QuestionBatch.Status.ACTIVE)
                .ifPresent(prev -> {
                    prev.setStatus(QuestionBatch.Status.EXPIRED);
                    batchRepo.save(prev);
                });

        // latest scheduled batch whose slot time has arrived
        Optional<QuestionBatch> nextOpt = batchRepo
                .findFirstBySegmentKeyAndScheduledSlotTimeLessThanEqualOrderByScheduledSlotTimeDesc(segmentKey, now)
                .filter(b -> b.getStatus() == QuestionBatch.Status.SCHEDULED);

        if (nextOpt.isEmpty()) {
            redis.delete(ACTIVE_BATCH_KEY_PREFIX + segmentKey);
            return null; // stock ran out — the stock-check job will have alerted admin
        }

        QuestionBatch batch = nextOpt.get();
        batch.setStatus(QuestionBatch.Status.ACTIVE);
        batchRepo.save(batch);

        // mark any older scheduled-but-missed batches expired (e.g. after downtime)
        List<QuestionBatch> stale = batchRepo.findByStatusAndScheduledSlotTimeLessThanEqual(
                QuestionBatch.Status.SCHEDULED, now);
        for (QuestionBatch s : stale) {
            if (s.getSegmentKey().equals(segmentKey) && !s.getId().equals(batch.getId())) {
                s.setStatus(QuestionBatch.Status.EXPIRED);
                batchRepo.save(s);
            }
        }

        cacheActiveBatch(batch);
        return batch;
    }

    public void cacheActiveBatch(QuestionBatch batch) {
        try {
            long ttl = QuizTimeUtil.secondsToNextSlot(LocalDateTime.now());
            redis.set(ACTIVE_BATCH_KEY_PREFIX + batch.getSegmentKey(),
                    objectMapper.writeValueAsString(batch), ttl);
        } catch (Exception ignored) {
            // cache write failure is non-fatal — DB fallback covers reads
        }
    }

    /**
     * Cache-miss fallback (Section 11.1): Redis first, DB second, repopulate.
     * Used by Web/App on quiz start.
     */
    public QuestionBatch getActiveBatch(String segmentKey) {
        String cached = redis.get(ACTIVE_BATCH_KEY_PREFIX + segmentKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, QuestionBatch.class);
            } catch (Exception ignored) {}
        }
        QuestionBatch batch = batchRepo
                .findFirstBySegmentKeyAndStatus(segmentKey, QuestionBatch.Status.ACTIVE)
                .orElse(null);
        if (batch == null) {
            // nothing marked ACTIVE (e.g. fresh deploy) — derive from slot time
            batch = batchRepo
                    .findFirstBySegmentKeyAndScheduledSlotTimeLessThanEqualOrderByScheduledSlotTimeDesc(
                            segmentKey, LocalDateTime.now())
                    .orElse(null);
        }
        if (batch != null) cacheActiveBatch(batch);
        return batch;
    }
}
