package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.model.ProblemReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/reports")
public class ProblemReportController {

    @Autowired
    private ProblemReportService service;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createReport(
            @RequestPart("data") String data,
            @RequestPart(value = "files", required = false) MultipartFile[] files
    ) throws Exception {

        ProblemReportRequest req = objectMapper.readValue(data, ProblemReportRequest.class);
        ProblemReport saved = service.createReport(req, files);
        return ResponseEntity.ok(saved);
    }
}

