package com.smartgaon.ai.smartgaon_api.sewa.jobs;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employerId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String requirements;
    private String salaryRange;
    private String employmentType;
    private String location;
    private String contactNumber;
    private String deadline;
    private String status;

    private LocalDateTime createdAt = LocalDateTime.now();
}
