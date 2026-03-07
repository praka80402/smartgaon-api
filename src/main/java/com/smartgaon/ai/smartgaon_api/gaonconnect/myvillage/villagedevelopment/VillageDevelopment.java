package com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.villagedevelopment;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.Village;
import com.smartgaon.ai.smartgaon_api.gaonconnect.myvillage.development.Development;

@Entity
@Data
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

    private Integer progressPercent;

    private String remarks;

    // ✅ Gallery Images (Max 20)
    @ElementCollection
    @CollectionTable(
            name = "village_development_gallery",
            joinColumns = @JoinColumn(name = "village_development_id")
    )
    @Column(name = "image_url")
    private List<String> galleryImages;
    
    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;
    
    @ElementCollection
    @CollectionTable(
            name = "village_development_reports",
            joinColumns = @JoinColumn(name = "village_development_id")
    )
    @Column(name = "report_url")
    private List<String> reports;
    
}