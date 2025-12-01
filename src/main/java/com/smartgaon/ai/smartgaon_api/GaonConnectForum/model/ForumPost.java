
package com.smartgaon.ai.smartgaon_api.GaonConnectForum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.smartgaon.ai.smartgaon_api.model.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "forumposts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ForumPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    // Author (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    @JsonIgnoreProperties({"password", "otp", "otpExpiry", "profileImage"})
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String category;

    // ⭐ REMOVED TAGS
    // private Set<String> tags = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "post_media", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "media_url")
    private List<String> mediaAttachments = new ArrayList<>();
    

    // ⭐ NEW FIELD — FETCHED FROM USER AUTOMATICALLY
    @Column(nullable = true)
    private String area;
    
    @ElementCollection
    @CollectionTable(
            name = "post_likes",
            joinColumns = @JoinColumn(name = "post_id")
    )
    @Column(name = "user_id")
    private Set<Long> likedUsers = new HashSet<>();

    private Long likeCount = 0L;

    private Long commentCount = 0L;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    public enum Status { ACTIVE, MODERATED, DELETED }
}
