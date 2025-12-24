package com.smartgaon.ai.smartgaon_api.sewa.jobhistory;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class WorkerReviewResponse {

    private Double averageRating;
    private int totalReviews;
    private List<ReviewItem> reviews;

    @Data
    @AllArgsConstructor
    public static class ReviewItem {
        private Long jobId;
        private String jobTitle;
        private Integer rating;
        private String comment;
        private String employerName;
        private LocalDateTime createdAt;
    }
}
