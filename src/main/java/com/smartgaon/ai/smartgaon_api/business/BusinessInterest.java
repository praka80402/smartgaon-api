package com.smartgaon.ai.smartgaon_api.business;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "business_interest",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"business_id", "user_id"})
        }
)
@Getter
@Setter
public class BusinessInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long businessId;
    private Long userId;

    private String name;
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
