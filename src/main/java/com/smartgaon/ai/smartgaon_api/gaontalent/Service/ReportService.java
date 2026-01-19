package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.*;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentReport;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentReportRepository;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TalentReportRepository reportRepo;

    public String reportPost(
            Long entryId,
            Long userId,
            TalentReportReason reason,
            String customReason
    ) {

        if (reportRepo.existsByEntryIdAndUserId(entryId, userId)) {
            return "Already reported";
        }

        if (reason == TalentReportReason.OTHER &&
                (customReason == null || customReason.trim().isEmpty())) {
            throw new RuntimeException("Custom reason required for OTHER");
        }

        TalentReport report = new TalentReport();
        report.setEntryId(entryId);
        report.setUserId(userId);
        report.setReason(reason);
        report.setCustomReason(reason == TalentReportReason.OTHER ? customReason : null);

        reportRepo.save(report);
        return "Post reported successfully";
    }

    public boolean isReportedByUser(Long entryId, Long userId) {
        return reportRepo.existsByEntryIdAndUserId(entryId, userId);
    }
}


