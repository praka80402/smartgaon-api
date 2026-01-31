package com.smartgaon.ai.smartgaon_api.ncertsyllabus.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}
