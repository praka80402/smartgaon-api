package com.smartgaon.ai.smartgaon_api.schoolcompetition.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prize_distribution_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrizeDistributionVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "competition_id", nullable = false)
    private String competitionId;

    @Column(name = "competition_name")
    private String competitionName;

    @Column(name = "category")
    private String category;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "video_url", columnDefinition = "TEXT", nullable = false)
    private String videoUrl;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}