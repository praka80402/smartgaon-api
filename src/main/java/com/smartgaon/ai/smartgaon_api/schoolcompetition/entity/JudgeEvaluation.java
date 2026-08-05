package com.smartgaon.ai.smartgaon_api.schoolcompetition.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "judge_evaluations", uniqueConstraints = {
    @UniqueConstraint(name = "uk_submission_judge", columnNames = {"submission_id", "judge_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JudgeEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submission_id", nullable = false, length = 64)
    private String submissionId;

    @Column(name = "judge_id", nullable = false, length = 64)
    private String judgeId;

    @Column(name = "appearance_score", nullable = false)
    private Integer appearanceScore;

    @Column(name = "content_score", nullable = false)
    private Integer contentScore;

    @Column(name = "confidence_score", nullable = false)
    private Integer confidenceScore;

    @Column(name = "criteria4_score", nullable = false)
    private Integer criteria4Score;

    @Column(name = "criteria5_score", nullable = false)
    private Integer criteria5Score;

    @Column(name = "total_score", nullable = false)
    private Integer totalScore;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String remarks; // Mandatory remarks/description

    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
