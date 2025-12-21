package com.smartgaon.ai.smartgaon_api.gaontalent.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class TalentCompetition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private boolean active = true;
}
