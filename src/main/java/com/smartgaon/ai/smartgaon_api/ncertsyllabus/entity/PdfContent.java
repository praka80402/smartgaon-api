package com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PdfContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String pdfUrl;

    @ManyToOne
    private Chapter chapter;
}
