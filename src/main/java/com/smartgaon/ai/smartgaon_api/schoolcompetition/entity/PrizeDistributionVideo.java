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

    @Column(name = "video_year")
    private String year;

    @Column(name = "video_month")
    private String month;

    @Column(name = "competition_type")
    private String competitionType;

    @Column(name = "group_category")
    private String groupCategory;

    @Column(name = "is_past_competition")
    @Builder.Default
    private Boolean isPastCompetition = false;

    @Column(name = "winner_rank")
    private Integer winnerRank;

    @Column(name = "prize_amount")
    private String prizeAmount;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "school_name")
    private String schoolName;

    @Column(name = "student_class")
    private String studentClass;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "is_consolation")
    @Builder.Default
    private Boolean isConsolation = false;

    @Column(name = "show_on_web")
    @Builder.Default
    private Boolean showOnWeb = true;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}