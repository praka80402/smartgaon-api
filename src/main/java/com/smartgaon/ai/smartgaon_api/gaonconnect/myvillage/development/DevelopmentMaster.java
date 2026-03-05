package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "development_master")
public class DevelopmentMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String title;

    @Column(length = 5000)
    private String imageUrl;
}
