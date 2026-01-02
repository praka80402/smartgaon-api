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

    @Column(length = 30)
    private String state;

    @Column(length = 30)
    private String district;

    @Column(length = 50)
    private String area;

    @Column(length = 6)
    private String pincode;

    @Column(length = 20)
    private String roles;

    private String occupation;

    @Column(length = 1000)
    private String note;

    @Column(name = "profile_image_url", length = 1000)
    private String profileImageUrl;

    private String otp;
    private LocalDateTime otpExpiry;
    private boolean verified = false;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "deleted_by")
    private String deletedBy; // "USER" or "ADMIN"

    private LocalDateTime deletedAt;
    
    @Column(name = "account_enabled")
    private Boolean accountEnabled = true;
}
