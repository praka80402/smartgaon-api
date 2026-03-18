package com.smartgaon.ai.smartgaon_api.QA.controller;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.QA.model.QA;
import com.smartgaon.ai.smartgaon_api.QA.service.QAService;



@RestController
@RequestMapping("/api/admin/qa")
//@CrossOrigin("*")
public class QAController {

    @Autowired
    private QAService service;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            service.uploadExcel(file);
            return "File Uploaded Successfully (Duplicates Skipped)";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/search")
    public List<QA> searchQuestion(@RequestParam String question) {
        return service.search(question);
    }

    @GetMapping("/all")
    public List<QA> getAllQA() {
        return service.getAllQA();
    }
}
