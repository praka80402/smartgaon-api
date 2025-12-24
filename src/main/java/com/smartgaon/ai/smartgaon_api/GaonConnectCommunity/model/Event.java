package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//@Entity
//@Table(name = "community_events")
//@Getter
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Event {
//
//    @Id
//    @GeneratedValue
//    private UUID id;
//
//    private String title;
//    private String description;
//    private LocalDateTime startDateTime;
//    private LocalDateTime endDateTime;
//    private String location;
//    private String contactInfo;
//
//    @Column(length = 1000)
//    private String pictureUrl;
//
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    public void onCreate() {
//        createdAt = LocalDateTime.now();
//    }
//}


@Entity
@Table(name = "community_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String description;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String location;
    private String contactInfo;

    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "image_url", length = 1000)
    private List<String> imageUrls = new ArrayList<>();

    private String videoUrl;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
