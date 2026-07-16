package com.smartgaon.ai.smartgaon_api.shikshaquiz.dto;

import java.util.List;

import com.smartgaon.ai.smartgaon_api.shikshaquiz.model.Question;

/** DTOs for question APIs. */
public class QuestionDtos {

    /**
     * Question as sent to Web/App users — NOTE: correctOption is intentionally
     * absent. Correct answers are validated server-side only (Section 11).
     */
    public static class QuestionPublicDto {
        public Long id;
        public String subject;
        public String questionText;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;

        public static QuestionPublicDto from(Question q) {
            QuestionPublicDto dto = new QuestionPublicDto();
            dto.id = q.getId();
            dto.subject = q.getSubject();
            dto.questionText = q.getQuestionText();
            dto.optionA = q.getOptionA();
            dto.optionB = q.getOptionB();
            dto.optionC = q.getOptionC();
            dto.optionD = q.getOptionD();
            return dto;
        }
    }

    /** A group of similar questions shown as a card in the admin duplicate-resolution UI. */
    public static class DuplicateGroupDto {
        public List<DuplicateEntryDto> questions;
    }

    public static class DuplicateEntryDto {
        public Long id;
        public String questionText;
        public String subject;
        public String classLevel;
        public String competitionType;
        public double similarityScore; // vs the first question in the group
    }

    /** Result of a bulk Excel/CSV upload. */
    public static class BulkUploadResultDto {
        public int inserted;
        public int duplicatesSkipped;
        public int invalidRowsSkipped;
        public String message;
    }
}
