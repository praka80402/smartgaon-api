package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sg_village_development")
@Data
public class SgVillageDevelopment {

    @Id
    private Long id;

    private Long developmentId;
    private Integer progressPercent;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sg_vdev_images", joinColumns = @JoinColumn(name = "vdev_id"))
    @Column(name = "image", columnDefinition = "LONGTEXT")
    private List<String> images = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(columnDefinition = "LONGTEXT")
    private String document;
}
