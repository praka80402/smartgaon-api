package com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "school_class")
public class SchoolClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Integer classNumber; // 1 to 12
}
