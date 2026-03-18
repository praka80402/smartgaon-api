package com.smartgaon.ai.smartgaon_api.QA.service;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.smartgaon.ai.smartgaon_api.QA.model.QA;
import com.smartgaon.ai.smartgaon_api.QA.repository.QARepository;

@Service
public class QAService {

    @Autowired
    private QARepository repo;

    public void uploadExcel(MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {

            if (row.getRowNum() == 0) continue;

            if (row.getCell(0) == null || row.getCell(1) == null) continue;

            String question = row.getCell(0).toString().trim();
            String answer = row.getCell(1).toString().trim();

            if (question.isEmpty() || answer.isEmpty()) continue;

            // ✅ Avoid duplicate
            if (repo.existsByQuestion(question)) continue;

            QA qa = new QA();
            qa.setQuestion(question);
            qa.setAnswer(answer);

            repo.save(qa);
        }

        workbook.close();
    }

    public List<QA> search(String question) {
        return repo.searchByQuestion(question);
    }

    public List<QA> getAllQA() {
        return repo.findAll();
    }
}