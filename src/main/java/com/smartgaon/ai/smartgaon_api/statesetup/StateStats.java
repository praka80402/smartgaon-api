package com.smartgaon.ai.smartgaon_api.statesetup;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "state_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StateStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String stateName;

    private Integer count;
}