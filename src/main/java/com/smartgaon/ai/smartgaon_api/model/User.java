//package com.smartgaon.ai.smartgaon_api.model;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Id;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Table;
//
//@Entity
//public class User {
//    @Id
//    @GeneratedValue
//    private Long id;
//    private String email;
//    private String password;
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }
//}


package com.smartgaon.ai.smartgaon_api.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Data                 
@NoArgsConstructor     
@AllArgsConstructor                  
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;

    private String phone;
    private String password;
    @Column(name = "reset_token")
    private String resetToken;
   

    @Column(nullable = true)
    private String village;

    @Column(nullable = true, length = 500)
    private String bio;

    @Lob
    @JsonIgnore
    private byte[] profileImage;

    @Column(nullable = true, length = 100)
    private String occupation;
    
    //  link posts to users 
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> posts;

    // link comments to users 
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comments;
}
























