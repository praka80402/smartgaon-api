package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "village_requests")
public class VillageRequest {

    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String district;
    private String state;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String submitterName;
    private String submitterPhone;

    /** URLs of village images saved on the server. */
    @ElementCollection
    @CollectionTable(name = "village_request_images", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "image_url", length = 500)
    private List<String> images = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VillageRequestPlace> places = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    /** Reason the admin gives when rejecting (optional). */
    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    /** Id of the Village created from this request when approved. */
    private Long createdVillageId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant reviewedAt;

    // ── getters / setters ──
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubmitterName() { return submitterName; }
    public void setSubmitterName(String submitterName) { this.submitterName = submitterName; }
    public String getSubmitterPhone() { return submitterPhone; }
    public void setSubmitterPhone(String submitterPhone) { this.submitterPhone = submitterPhone; }
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
    public List<VillageRequestPlace> getPlaces() { return places; }
    public void setPlaces(List<VillageRequestPlace> places) { this.places = places; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public Long getCreatedVillageId() { return createdVillageId; }
    public void setCreatedVillageId(Long createdVillageId) { this.createdVillageId = createdVillageId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
}
