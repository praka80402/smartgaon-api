package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import jakarta.persistence.*;

@Entity
@Table(name = "village_request_places")
public class VillageRequestPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** URL of the photo file on the server. */
    @Column(length = 500)
    private String photo;

    private String videoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private VillageRequest request;

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public VillageRequest getRequest() { return request; }
    public void setRequest(VillageRequest request) { this.request = request; }
}
