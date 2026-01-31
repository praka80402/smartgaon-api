package com.smartgaon.ai.smartgaon_api.ncertsyllabus.service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity.*;
public interface NcertUserService {
	 List<SchoolClass> getClasses();

	    List<Subject> getSubjects(Long classId);

	    List<Chapter> getChapters(Long subjectId);

	    Page<PdfContent> getPdfs(Long chapterId, int page, int size);

	    Page<VideoContent> getVideos(Long chapterId, int page, int size);

}
