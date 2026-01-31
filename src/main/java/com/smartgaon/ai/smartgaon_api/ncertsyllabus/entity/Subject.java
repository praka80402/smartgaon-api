package com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass;
}

