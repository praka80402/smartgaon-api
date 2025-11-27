package com.smartgaon.ai.smartgaon_api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_phone", columnList = "phone"),
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_pincode", columnList = "pincode")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(unique = true, length = 15)
    private String phone;

    private String password;

    @Column(name = "reset_token")
    private String resetToken;

    // -----------------------------
    // LOCATION FIELDS
    // -----------------------------
    @Column(length = 20)
    private String village;

    @Column(length = 6)
    private String pincode;

    // -----------------------------
    // ROLES (String ✅)
    // -----------------------------
    @Column(length = 20)
    private String roles;

    // -----------------------------
    // IMAGE (CLOUDINARY)
    // -----------------------------
    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    private String otp;
    private LocalDateTime otpExpiry;
    private boolean verified = false;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;
}
