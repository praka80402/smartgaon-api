//-----------------------------------------------------------------------------
package com.smartgaon.ai.smartgaon_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
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

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String phone;

    private String password;

    @Column(name = "reset_token")
    private String resetToken;

    // -----------------------------
    // LOCATION FIELDS
    // -----------------------------
    @Column(length = 20)
    private String village;

    @Column(length = 50)
    private String district;

    @Column(length = 20)
    private String state;

    @Column(length = 6)
    private String pincode;

    @Column(length = 100)
    private String area;

    
    @Column(length = 20)
    private String roles;   

    // -----------------------------

    @Lob
    @JsonIgnore
    private byte[] profileImage;

    private String otp;
    private LocalDateTime otpExpiry;
    private boolean verified = false;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;
}
