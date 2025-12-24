////package com.smartgaon.ai.smartgaon_api.model;
////
////import jakarta.persistence.*;
////import lombok.*;
////import java.time.LocalDateTime;
////import java.util.List;
////
////import com.fasterxml.jackson.annotation.JsonBackReference;
////import com.fasterxml.jackson.annotation.JsonManagedReference;
////
////@Entity
////@Table(name = "posts")
////@Data
////@NoArgsConstructor
////@AllArgsConstructor
////public class Post {
////    @Id
////    @GeneratedValue(strategy = GenerationType.IDENTITY)
////    private Long id;
////
////    private String content;
////    private String authorEmail;
////    private String authorName;
////    private LocalDateTime createdAt = LocalDateTime.now();
////
////    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
////    @JsonManagedReference
////    private List<Comment> comments;
////    
////    @ManyToOne(fetch = FetchType.LAZY)
////    @JoinColumn(name = "user_id")
////    @JsonBackReference
////    private User user;
////}
////
//
//
//
//
//
//package com.smartgaon.ai.smartgaon_api.post.model;
//
//import jakarta.persistence.*;
//import lombok.*;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import com.fasterxml.jackson.annotation.JsonManagedReference;
//import com.smartgaon.ai.smartgaon_api.comment.model.Comment;
//import com.smartgaon.ai.smartgaon_api.model.User;
//
//@Entity
//@Table(name = "posts")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class Post {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(length = 250)
//    private String content;
//
//    private String authorEmail;
//    private String authorName;
//    private LocalDateTime createdAt = LocalDateTime.now();
//    
//    private int likesCount = 0;
//    private int repostsCount = 0;
//    
//    @ManyToMany
//    @JoinTable(
//        name = "post_likes",
//        joinColumns = @JoinColumn(name = "post_id"),
//        inverseJoinColumns = @JoinColumn(name = "user_id")
//    )
//    private Set<User> likedBy = new HashSet<>();
//    
////    private byte imageUrl;   
////    private String videoUrl;   
//
//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
//    @JsonManagedReference
//    private List<Comment> comments;
//
//    
////    @ManyToOne(fetch = FetchType.LAZY)
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id")
////    @JsonBackReference
//    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
//    private User user;
//
//	
//}
//
//
//
//
//
//
//
//
//
//
//
//
//
