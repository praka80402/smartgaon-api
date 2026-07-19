package com.smartgaon.ai.smartgaon_api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayTip {
    private String id;
    private String title;
    private String category;
    private String description;
    private String imageUrl;
    private String targetDate; // Format: YYYY-MM-DD
    private Long createdAt;
}
