package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.ProblemReport;
import com.smartgaon.ai.smartgaon_api.model.User;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProblemReportService {

    private final S3Service s3Service;
    private final ProblemReportRepository reportRepo;
    private final UserRepository userRepo;

    public ProblemReportService(S3Service s3Service,
                                ProblemReportRepository reportRepo,
                                UserRepository userRepo) {
        this.s3Service = s3Service;
        this.reportRepo = reportRepo;
        this.userRepo = userRepo;
    }

    public ProblemReport createReport(ProblemReportRequest req, MultipartFile[] files) {

        // 1️⃣ Find reporter
        User reporter = userRepo.findById(req.getReporterId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Reporter not found: " + req.getReporterId()));

        // 2️⃣ Upload files to S3
        List<String> urls = new ArrayList<>();
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                try {
                    String url = s3Service.uploadFile(file);
                    if (StringUtils.isNotBlank(url)) {
                        urls.add(url);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("S3 upload failed", e);
                }
            }
        }

        // 3️⃣ Save problem report
        ProblemReport report = new ProblemReport();
        report.setReporter(reporter);
        report.setCategory(req.getCategory());
        report.setTitle(req.getTitle());
        report.setDescription(req.getDescription());
        report.setLocation(req.getLocation());
        report.setMediaAttachments(urls);
        report.setPublic(req.getIsPublic() == null || req.getIsPublic());
        report.setStatus("Submitted");

        return reportRepo.save(report);
    }
}
