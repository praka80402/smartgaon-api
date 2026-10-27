package com.smartgaon.ai.smartgaon_api.mandi.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MandiTrendDTO {

    private String commodity;
    private String commodityHi;
    private String district;
    private String state;
    private Integer periodDays;
    private Double currentModalPrice;
    private Double priceChange;
    private String trendDirection; // "UP", "DOWN", "STABLE"
    private String percentageChange; // e.g. "+3.19%" or "-1.25%"
    private List<WeeklyBucket> weeklyBuckets;
    private List<DailyHistory> dailyHistory;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WeeklyBucket {
        private String week;        // e.g. "Week 1", "Week 2", "Today"
        private String dateRange;   // e.g. "25 Sep - 01 Oct"
        private Double avgModal;
        private String height;      // e.g. "75%", "90%"
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyHistory {
        private String date;        // "yyyy-MM-dd"
        private Double modal;
        private Double min;
        private Double max;
        private String market;
    }
}
