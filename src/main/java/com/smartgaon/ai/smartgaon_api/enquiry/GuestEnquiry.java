package com.smartgaon.ai.smartgaon_api.enquiry;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guest_enquiries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuestEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Column(nullable = false)
    private Integer guests;

    private String roomType;

    private String specialRequest;

    @Builder.Default
    private String status = "NEW";

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}