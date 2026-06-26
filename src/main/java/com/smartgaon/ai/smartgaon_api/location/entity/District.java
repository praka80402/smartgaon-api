package com.smartgaon.ai.smartgaon_api.location.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "districts")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class District {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "state_id", nullable = false)
    private Long stateId;
}