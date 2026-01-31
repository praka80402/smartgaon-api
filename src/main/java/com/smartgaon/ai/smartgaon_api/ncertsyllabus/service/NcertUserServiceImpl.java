package com.smartgaon.ai.smartgaon_api.ncertsyllabus.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.*;
import com.smartgaon.ai.smartgaon_api.ncertsyllabus.repository.*;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NcertUserServiceImpl implements NcertUserService {

    private final SchoolClassRepo classRepo;
    private final SubjectRepo subjectRepo;
    private final ChapterRepo chapterRepo;
    private final PdfRepo pdfRepo;
    private final VideoRepo videoRepo;


    @Override
    public List<SchoolClass> getClasses() {
        return classRepo.findAll();
    }

    @Override
    public List<Subject> getSubjects(Long classId) {
    	 return subjectRepo.findBySchoolClass_Id(classId);
    }

    @Override
    public List<Chapter> getChapters(Long subjectId) {
        return chapterRepo.findBySubjectId(subjectId);
    }

    @Override
    public Page<PdfContent> getPdfs(Long chapterId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return pdfRepo.findByChapterId(chapterId, pageable);
    }

    @Override
    public Page<VideoContent> getVideos(Long chapterId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return videoRepo.findByChapterId(chapterId, pageable);
    }
}
