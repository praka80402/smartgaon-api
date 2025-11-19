package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import com.cloudinary.Cloudinary;

import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.ProblemReport;
import com.smartgaon.ai.smartgaon_api.model.User;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.lang3.StringUtils;
import com.cloudinary.utils.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProblemReportService {

    private final Cloudinary cloudinary;
    private final ProblemReportRepository reportRepo;
    private final UserRepository userRepo;

    public ProblemReportService(Cloudinary cloudinary,
                                ProblemReportRepository reportRepo,
                                UserRepository userRepo) {
        this.cloudinary = cloudinary;
        this.reportRepo = reportRepo;
        this.userRepo = userRepo;
    }

    public ProblemReport createReport(ProblemReportRequest req, MultipartFile[] files) {
        // find reporter user (throw if not found)
        User reporter = userRepo.findById(req.getReporterId())
                .orElseThrow(() -> new IllegalArgumentException("Reporter not found: " + req.getReporterId()));

        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                try {
                    Map<?,?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                            "folder", "problem_reports"
                    ));
                    Object secureUrl = uploadResult.get("secure_url");
                    if (secureUrl != null && StringUtils.isNotBlank(secureUrl.toString())) {
                        urls.add(secureUrl.toString());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Cloudinary upload failed", e);
                }
            }
        }

        ProblemReport report = new ProblemReport();
        report.setReporter(reporter);
        report.setCategory(req.getCategory());
        report.setTitle(req.getTitle());
        report.setDescription(req.getDescription());
        report.setLocation(req.getLocation());
        report.setMediaAttachments(urls);
        report.setPublic(req.getIsPublic() == null ? true : req.getIsPublic());
        report.setStatus("Submitted");

        return reportRepo.save(report);
    }

    // other methods: list, getById, updateStatus, assignTo etc.
}
