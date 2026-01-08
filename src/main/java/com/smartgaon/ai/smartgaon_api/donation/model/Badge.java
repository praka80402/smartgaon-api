package com.smartgaon.ai.smartgaon_api.donation.model;

import com.smartgaon.ai.smartgaon_api.model.User;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "badges",
    indexes = {
        @Index(name = "idx_badge_user", columnList = "userId")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    private Long userId;
    private String badgeName;
    private String reason;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
}


