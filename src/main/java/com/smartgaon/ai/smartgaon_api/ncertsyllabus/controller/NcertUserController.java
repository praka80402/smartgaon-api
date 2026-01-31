package com.smartgaon.ai.smartgaon_api.ncertsyllabus.controller;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.*;
import com.smartgaon.ai.smartgaon_api.ncertsyllabus.service.NcertUserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user/ncert")
@RequiredArgsConstructor
public class NcertUserController {

    private final NcertUserService userService;


    // ================= CLASSES =================
    @GetMapping("/classes")
    public List<SchoolClass> getClasses() {
        return userService.getClasses();
    }


    // ================= SUBJECTS =================
    @GetMapping("/subjects/{classId}")
    public List<Subject> getSubjects(@PathVariable Long classId) {
        return userService.getSubjects(classId);
    }


    // ================= CHAPTERS =================
    @GetMapping("/chapters/{subjectId}")
    public List<Chapter> getChapters(@PathVariable Long subjectId) {
        return userService.getChapters(subjectId);
    }


    // ================= PDF =================
    @GetMapping("/pdf/{chapterId}")
    public Page<PdfContent> getPdfs(
            @PathVariable Long chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return userService.getPdfs(chapterId, page, size);
    }


    // ================= VIDEO =================
    @GetMapping("/video/{chapterId}")
    public Page<VideoContent> getVideos(
            @PathVariable Long chapterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return userService.getVideos(chapterId, page, size);
    }
}
