package com.smartgaon.ai.smartgaon_api.location.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "states")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "state_code")
    private String stateCode;
}