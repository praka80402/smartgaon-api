package com.smartgaon.ai.smartgaon_api.shikshaquiz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quiz_question_batch", indexes = {
        @Index(name = "idx_batch_segment_slot", columnList = "segmentKey, scheduledSlotTime"),
        @Index(name = "idx_batch_status", columnList = "status")
})
public class QuestionBatch {

    public enum Status { SCHEDULED, ACTIVE, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // e.g. "CLASS_10_MATH" or "SSC"
    @Column(nullable = false, length = 100)
    private String segmentKey;

    // exact datetime this batch activates
    @Column(nullable = false)
    private LocalDateTime scheduledSlotTime;

    // stored as comma-separated ids, exposed as List<Long>
    @Column(columnDefinition = "TEXT", nullable = false)
    private String questionIdsCsv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Status status = Status.SCHEDULED;

    // GETTERS & SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSegmentKey() { return segmentKey; }
    public void setSegmentKey(String segmentKey) { this.segmentKey = segmentKey; }

    public LocalDateTime getScheduledSlotTime() { return scheduledSlotTime; }
    public void setScheduledSlotTime(LocalDateTime scheduledSlotTime) { this.scheduledSlotTime = scheduledSlotTime; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public List<Long> getQuestionIds() {
        List<Long> ids = new ArrayList<>();
        if (questionIdsCsv == null || questionIdsCsv.isBlank()) return ids;
        for (String s : questionIdsCsv.split(",")) {
            if (!s.isBlank()) ids.add(Long.parseLong(s.trim()));
        }
        return ids;
    }

    public void setQuestionIds(List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (Long qid : ids) {
            if (sb.length() > 0) sb.append(",");
            sb.append(qid);
        }
        this.questionIdsCsv = sb.toString();
    }
}
