package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class TalentVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long userId;      
    private String username;
    private String title;
    private String url;           // Cloudinary URL
    private String uploadedBy;

    private boolean isReel;       // true = reel (max 60 sec), false = long video (max 5 min)
    private long fileSize;       
    private String format;        

    private int likes = 0;
    private int comments = 0;
    private int shares = 0;

    private LocalDateTime uploadedAt = LocalDateTime.now();
}

