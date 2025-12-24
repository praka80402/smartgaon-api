package com.smartgaon.ai.smartgaon_api.gaonconnect.problemreport;

import com.smartgaon.ai.smartgaon_api.model.ProblemReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemReportRepository extends JpaRepository<ProblemReport, Long> {
}