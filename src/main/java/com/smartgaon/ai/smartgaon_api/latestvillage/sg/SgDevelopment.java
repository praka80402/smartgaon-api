package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import jakarta.persistence.*;
import lombok.Data;

/* The catalogue item (phase + title). Read-only from sg_development. */
@Entity
@Table(name = "sg_development")
@Data
public class SgDevelopment {

    @Id
    private Long id;

    private Integer phaseNumber;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String image;
}
