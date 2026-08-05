package com.smartgaon.ai.smartgaon_api.schoolcompetition.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "school_competitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolCompetition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competition_id", nullable = false, unique = true, length = 50)
    private String competitionId; // e.g. COMP-2026-AUG-001

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String category; // Public Speaking, Science Project, Kojo, etc.

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "verification_code", nullable = false, length = 50)
    private String verificationCode; // Single shared code for all participating schools

    @Builder.Default
    @Column(name = "is_live", nullable = false)
    private Boolean isLive = true;

    @Builder.Default
    @Column(name = "winner_announcement_mode", nullable = false, length = 20)
    private String winnerAnnouncementMode = "AUTOMATIC"; // AUTOMATIC or MANUAL

    @Builder.Default
    @Column(nullable = false, length = 20)
    private String status = "LIVE"; // DRAFT, LIVE, EVALUATION, COMPLETED

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
