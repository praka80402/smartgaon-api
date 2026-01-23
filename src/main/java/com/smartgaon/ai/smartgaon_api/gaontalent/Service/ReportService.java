package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentReport;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentReportRepository;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TalentReportRepository reportRepo;
    private final TalentEntryRepository entryRepo;

    public String reportPost(
            Long entryId,
            Long userId,
            TalentReportReason reason,
            String customReason
    ) {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // ❌ Cannot report own post
        if (entry.getUserId().equals(userId)) {
            throw new RuntimeException("You cannot report your own post");
        }

        // ❌ Already reported
        if (reportRepo.existsByEntryIdAndUserId(entryId, userId)) {
            return "Already reported";
        }

        if (reason == TalentReportReason.OTHER &&
                (customReason == null || customReason.trim().isEmpty())) {
            throw new RuntimeException("Reason required");
        }

        TalentReport report = new TalentReport();
        report.setEntryId(entryId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setCustomReason(
            reason == TalentReportReason.OTHER ? customReason : null
        );

        reportRepo.save(report);

        // Count total reports
        long total = reportRepo.countByEntryId(entryId);

        // Global block
        if (total >= 10) {
            entry.setBlocked(true);
            entryRepo.save(entry);
        }

        return "Reported successfully";
    }


    public boolean isReportedByUser(Long entryId, Long userId) {
        return reportRepo.existsByEntryIdAndUserId(entryId, userId);
    }
}


