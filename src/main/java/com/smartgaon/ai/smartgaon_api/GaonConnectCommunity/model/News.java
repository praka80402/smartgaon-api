package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "news")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String title;

    @Column(length = 500)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author;
    private String thumbnailUrl;
    private LocalDateTime publishedAt;

    @PrePersist
    public void onCreate() {
        publishedAt = LocalDateTime.now();
    }
}

