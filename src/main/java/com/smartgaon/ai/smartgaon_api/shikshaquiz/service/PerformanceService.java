package com.smartgaon.ai.smartgaon_api.shikshaquiz.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuizDtos.AttemptSummaryDto;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuizDtos.OverallStatsDto;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.dto.QuizDtos.PerformanceResponse;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.QuizAttempt;
import com.smartgaon.ai.smartgaon_api.shikshaquiz.repository.QuizAttemptRepository;

/**
 * User performance dashboard — last 5 quizzes (Section 10).
 * Low-frequency per-user read; no caching required.
 */
@Service
public class PerformanceService {

    @Autowired
    private QuizAttemptRepository attemptRepo;

    public PerformanceResponse getPerformance(Long userId) {
        List<QuizAttempt> last5 = attemptRepo.findTop5ByUserIdAndEndTimeIsNotNullOrderByEndTimeDesc(userId);

        PerformanceResponse resp = new PerformanceResponse();
        resp.attempts = new ArrayList<>();

        double sumPct = 0;
        double bestScore = 0;
        List<Double> pctsNewestFirst = new ArrayList<>();

        for (QuizAttempt a : last5) {
            AttemptSummaryDto dto = new AttemptSummaryDto();
            dto.segmentKey = a.getSegmentKey();
            dto.score = a.getScore() != null ? a.getScore() : 0;
            dto.totalQuestions = a.getTotalQuestions() != null ? a.getTotalQuestions() : 0;
            dto.percentage = dto.totalQuestions == 0 ? 0
                    : Math.round(dto.score * 10000.0 / dto.totalQuestions) / 100.0;
            dto.date = a.getEndTime();
            resp.attempts.add(dto);

            sumPct += dto.percentage;
            bestScore = Math.max(bestScore, dto.percentage);
            pctsNewestFirst.add(dto.percentage);
        }

        OverallStatsDto stats = new OverallStatsDto();
        stats.avgPercentage = last5.isEmpty() ? 0 : Math.round(sumPct / last5.size() * 100.0) / 100.0;
        stats.bestScore = bestScore;
        stats.totalQuizzesTaken = attemptRepo.countByUserIdAndEndTimeIsNotNull(userId);
        stats.trend = computeTrend(pctsNewestFirst);
        resp.overallStats = stats;
        return resp;
    }

    /**
     * Trend = avg of the more recent half vs avg of the earlier half of the
     * same 5-attempt window (Section 10). ±5 percentage points = STABLE.
     */
    private String computeTrend(List<Double> pctsNewestFirst) {
        if (pctsNewestFirst.size() < 2) return "STABLE";
        int half = pctsNewestFirst.size() / 2;

        double recentSum = 0, earlierSum = 0;
        for (int i = 0; i < half; i++) recentSum += pctsNewestFirst.get(i);
        for (int i = half; i < pctsNewestFirst.size(); i++) earlierSum += pctsNewestFirst.get(i);

        double recentAvg = recentSum / half;
        double earlierAvg = earlierSum / (pctsNewestFirst.size() - half);

        if (recentAvg - earlierAvg > 5) return "IMPROVING";
        if (earlierAvg - recentAvg > 5) return "DECLINING";
        return "STABLE";
    }
}
