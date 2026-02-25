package com.smartgaon.ai.smartgaon_api.villageofficer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "government_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GovernmentOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;   // changed from offerName

    @Column(nullable = false)
    private String department;

    @Column(nullable = false)
    private String phone;  // changed from phoneNumber

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;

    private LocalDateTime createdAt = LocalDateTime.now();
}