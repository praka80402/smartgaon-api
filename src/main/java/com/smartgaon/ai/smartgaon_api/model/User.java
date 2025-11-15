
//
//package com.smartgaon.ai.smartgaon_api.model;
//
//import java.util.List;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Table(name = "users")
//@Data                 
//@NoArgsConstructor     
//@AllArgsConstructor
//@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY) 
//    private Long id;
//
//    private String firstName;
//    private String lastName;
//
//    @Column(unique = true)
//    private String email;
//
//    private String phone;
//    private String password;
//    @Column(name = "reset_token")
//    private String resetToken;
//   
//
//    @Column(nullable = true)
//    private String village;
//
//    @Column(nullable = true, length = 500)
//    private String bio;
//
//    @Lob
//    @JsonIgnore
//    private byte[] profileImage;
//
//    @Column(nullable = true, length = 100)
//    private String occupation;
//    
//    //  link posts to users 
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Post> posts;
//
//    // link comments to users 
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Comment> comments;
//}




//-----------------------------------------------------------------------------
package com.smartgaon.ai.smartgaon_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = true)
    private String village;

//    @Column(nullable = true, length = 500)
//    private String bio;

    @Lob
    @JsonIgnore
    private byte[] profileImage;

    @Column(nullable = true, length = 100)
    private String occupation;

    private String otp;
    private LocalDateTime otpExpiry;
    private boolean verified = false;
    
    
    @Column(length =6 )
    private String pincode;

    @Column(length = 100)
    private String area;

    @Column(name = "profile_completed")
    private boolean profileCompleted = false;

    
//
//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Post> posts;

//    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonIgnore
//    private List<Comment> comments;
}



















