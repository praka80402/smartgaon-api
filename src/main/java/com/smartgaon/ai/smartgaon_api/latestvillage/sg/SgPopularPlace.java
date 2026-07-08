package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sg_popular_place")
@Data
public class SgPopularPlace {

    @Id
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "LONGTEXT")
    private String photo;

    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "village_id")
    @JsonIgnore
    private Long villageId;
}
