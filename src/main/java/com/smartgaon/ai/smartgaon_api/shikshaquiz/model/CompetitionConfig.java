package com.smartgaon.ai.smartgaon_api.shikshaquiz.model;

import jakarta.persistence.*;

/**
 * Per-segment/competition tunables — stored in DB (not hardcoded)
 * so admin can change them without a code deployment.
 *
 * Also used for ACADEMIC segments (competitionType = "CLASS_5" .. "CLASS_12")
 * where the timer/attempt-limit fields stay null until team lead
 * confirms open items #1 and #2 of the spec.
 */
@Entity
@Table(name = "quiz_competition_config", uniqueConstraints = {
        @UniqueConstraint(columnNames = "competitionType")
})
public class CompetitionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "SSC", "GENERAL", "STATE_EXAM" ... or "CLASS_5".."CLASS_12"
    @Column(nullable = false, length = 50)
    private String competitionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SegmentType segmentType;

    // Academic default 10, Competition default 20
    @Column(nullable = false)
    private int questionCount;

    // null = no timer (Academic, pending confirmation)
    private Integer timeLimitMinutes;

    // e.g. 5 — grantable once per attempt (Competition)
    private Integer extraTimeAllowedMinutes;

    // null = unlimited (Academic, pending confirmation); Competition default 2
    private Integer dailyAttemptLimit;

    // GETTERS & SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompetitionType() { return competitionType; }
    public void setCompetitionType(String competitionType) { this.competitionType = competitionType; }

    public SegmentType getSegmentType() { return segmentType; }
    public void setSegmentType(SegmentType segmentType) { this.segmentType = segmentType; }

    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
    public void setTimeLimitMinutes(Integer timeLimitMinutes) { this.timeLimitMinutes = timeLimitMinutes; }

    public Integer getExtraTimeAllowedMinutes() { return extraTimeAllowedMinutes; }
    public void setExtraTimeAllowedMinutes(Integer extraTimeAllowedMinutes) { this.extraTimeAllowedMinutes = extraTimeAllowedMinutes; }

    public Integer getDailyAttemptLimit() { return dailyAttemptLimit; }
    public void setDailyAttemptLimit(Integer dailyAttemptLimit) { this.dailyAttemptLimit = dailyAttemptLimit; }
}
