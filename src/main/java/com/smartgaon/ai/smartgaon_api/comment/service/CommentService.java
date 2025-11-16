//package com.smartgaon.ai.smartgaon_api.comment.service;
//
//import com.smartgaon.ai.smartgaon_api.comment.model.Comment;
//import com.smartgaon.ai.smartgaon_api.comment.repository.CommentRepository;
//import com.smartgaon.ai.smartgaon_api.post.model.Post;
//import com.smartgaon.ai.smartgaon_api.post.repository.PostRepository;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class CommentService {
//
//    @Autowired
//    private CommentRepository commentRepo;
//
//    @Autowired
//    private PostRepository postRepo;
//
//    public Comment addComment(Long postId, Comment comment) {
//        Optional<Post> post = postRepo.findById(postId);
//        if (post.isEmpty()) {
//            throw new RuntimeException("Post not found");
//        }
//        comment.setPost(post.get());
//        return commentRepo.save(comment);
//    }
//
//    public List<Comment> getComments(Long postId) {
//        return commentRepo.findByPostIdOrderByCreatedAtAsc(postId);
//    }
//}
