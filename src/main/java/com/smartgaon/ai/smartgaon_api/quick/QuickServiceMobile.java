package com.smartgaon.ai.smartgaon_api.quick;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "quick_mobile_services")
public class QuickServiceMobile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String icon;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 150)
    private String sub;

    @Column(nullable = false, length = 200)
    private String path;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "is_custom", nullable = false)
    private Boolean isCustom = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}

interface QuickServiceMobileRepository extends JpaRepository<QuickServiceMobile, Long> {
    List<QuickServiceMobile> findByActiveTrueOrderByDisplayOrderAsc();
    List<QuickServiceMobile> findAllByOrderByDisplayOrderAsc();
}
class QuickServiceMobileRequest {
    @NotBlank(message = "icon is required")
    private String icon;

    @NotBlank(message = "label is required")
    private String label;

    private String sub;

    @NotBlank(message = "path is required")
    private String path;

    private Integer displayOrder;
    private Boolean active = true;
    private Boolean isCustom = true;

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Boolean getIsCustom() { return isCustom; }
    public void setIsCustom(Boolean isCustom) { this.isCustom = isCustom; }
}