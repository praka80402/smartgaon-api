package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class TalentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
//    private int age;
    private LocalDate dob;

    // ⭐ New: Village/Area
    private String villageOrArea;
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

 


    private boolean isWinner = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
