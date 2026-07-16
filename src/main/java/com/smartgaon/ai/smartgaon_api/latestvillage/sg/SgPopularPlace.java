package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sg_popular_place_photo", joinColumns = @JoinColumn(name = "popular_place_id"))
    @Column(name = "photo", columnDefinition = "LONGTEXT")
    @OrderColumn(name = "photo_order")
    private List<String> photos = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "village_id")
    @JsonIgnore
    private Long villageId;
}