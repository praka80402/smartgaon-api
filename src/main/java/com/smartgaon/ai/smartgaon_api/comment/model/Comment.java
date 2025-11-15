//package com.smartgaon.ai.smartgaon_api.comment.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.smartgaon.ai.smartgaon_api.model.User;
//import com.smartgaon.ai.smartgaon_api.post.model.Post;
//
//@Entity
//@Table(name = "comments")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Comment {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    private String text;
//    private String commenterEmail;
//    private String commenterName;
//    private LocalDateTime createdAt = LocalDateTime.now();
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id")
//    @JsonBackReference
//    private Post post;
//    
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//}
//
