package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;

import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.Village;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.Development;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VillageDevelopment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "village_id")
    private Village village;

    @ManyToOne
    @JoinColumn(name = "development_id")
    private Development development;

    @Column(columnDefinition = "TEXT")
    private String workDescription;

    @Column(columnDefinition = "TEXT")
    private String benefit;

    private Integer progressPercent;
}
