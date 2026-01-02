package com.smartgaon.ai.smartgaon_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problem_reports",
        indexes = {
                @Index(name = "idx_created_at", columnList = "createdAt")
        }
)
public class ProblemReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;
    private String category;
    private String title;

    @Column(length = 200)
    private String location;

    @Column(columnDefinition = "TEXT")
    private String description;
    // Cloudinary URLs
    @ElementCollection
    @CollectionTable(name = "problem_media", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "media_url", length = 1000)
    private List<String> mediaAttachments = new ArrayList<>();

    @Column(length = 30)
    private String status = "Submitted"; // enum recommended

    // optional assignedTo FK -> User (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    private boolean isPublic = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Long getReportId() {
        return this.reportId;
    }

    public void setReportId(final Long reportId) {
        this.reportId = reportId;
    }

    public User getReporter() {
        return this.reporter;
    }

    public void setReporter(final User reporter) {
        this.reporter = reporter;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<String> getMediaAttachments() {
        return this.mediaAttachments;
    }

    public void setMediaAttachments(final List<String> mediaAttachments) {
        this.mediaAttachments = mediaAttachments;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return this.assignedTo;
    }

    public void setAssignedTo(final User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public boolean isPublic() {
        return this.isPublic;
    }

    public void setPublic(final boolean aPublic) {
        this.isPublic = aPublic;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }
}

