package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.model.ProblemReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reports")
public class ProblemReportController {

    @Autowired
    private ProblemReportService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProblemReportRepository reportRepo;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(
            @RequestPart("data") String data,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws Exception {

        ProblemReportRequest req = objectMapper.readValue(data, ProblemReportRequest.class);
        ProblemReport saved = service.createReport(req, files);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/page")
    public ResponseEntity<?> getPaginatedReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<ProblemReport> pageResult = reportRepo.findAll(pageable);

        return ResponseEntity.ok(pageResult);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(
                reportRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Report not found"))
        );
    }

}

