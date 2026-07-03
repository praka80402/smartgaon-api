package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

/* Read-only mapping to the admin-created sg_village table. */
@Entity
@Table(name = "sg_village")
@Data
public class SgVillage {

    @Id
    private Long id;

    private String name;
    private String district;
    private String state;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sg_village_images", joinColumns = @JoinColumn(name = "village_id"))
    @Column(name = "image", columnDefinition = "LONGTEXT")
    private List<String> images = new ArrayList<>();

    private Boolean popularPlace = false;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "village_id")
    private List<SgPopularPlace> popularPlaces = new ArrayList<>();

    private Boolean smartGaon = false;
    private Boolean stayEnquiry = false;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "village_id")
    private List<SgVillageDevelopment> assignments = new ArrayList<>();
}
