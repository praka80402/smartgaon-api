package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class TalentEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int age;
    private String phone;

    private String profileImageUrl;
    private String mediaUrl;
    private String mediaType;   // IMAGE / VIDEO

    @Enumerated(EnumType.STRING)
    private TalentCategory category;

    private Long competitionId;   // Knowing which competition user participated in

    private String referenceNumber;

    private int likes = 0;
    private int comments = 0;

    private boolean isWinner = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}
