package com.smartgaon.ai.smartgaon_api.gaontalent.dto;



import lombok.Data;

@Data
public class TalentVideoResponse {
    private String id;
    private Long userId;
    private String username;
    private String title;
    private String url;
    private String profileImageUrl;
    private boolean isReel;
    private long fileSize;
    private String format;
    private int likes;
    private int comments;
    private int shares;
    private String uploadedAt;
}

