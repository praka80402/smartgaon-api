package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Data
public class TalentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Long userId;
//    private int age;
    private LocalDate dob;

    // ⭐ New: Village/Area
    private String villageOrArea;
    private String userPincode;
    private String phone;

    private String profileImageUrl;
    private String mediaUrl;
    private String mediaType;   // IMAGE / VIDEO

    @Enumerated(EnumType.STRING)
    private TalentCategory category;

    private boolean isCompetition = false;
    private Long competitionId;   // Knowing which competition user participated in

    private String referenceNumber;

    private Integer comments = 0;
    private Integer likes = 0;
    private String processingStatus;

    private String thumbnailUrl;

    private String lowQualityVideoUrl;
    private boolean blocked = false;

    private boolean isWinner = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
