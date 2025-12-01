package com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.Impl;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumComment;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.model.ForumPost;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumCommentRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.repository.ForumPostRepository;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumCommentService;
import com.smartgaon.ai.smartgaon_api.auth.repository.UserRepository;
import com.smartgaon.ai.smartgaon_api.model.User;

import java.util.List;

@Service
public class CommentServiceImpl implements ForumCommentService {

    @Autowired
    private ForumCommentRepository commentRepo;

    @Autowired
    private ForumPostRepository postRepo;

    @Autowired
    private UserRepository userRepo;

    private CommentResponse map(ForumComment c) {
        String fullName = c.getUser().getFirstName() + " " + c.getUser().getLastName();
       

        return new CommentResponse(
                c.getCommentId(),
                c.getPost().getPostId(),
                c.getUser().getId(),
                fullName,
                c.getContent(),
                c.getLikeCount(),
                c.getStatus().name(),
                c.getCreatedAt(),
               c.getUser().getProfileImageUrl()
        );
    }

    @Override
    @Transactional
    public CommentResponse addComment(CommentCreateDto dto) {

        User user = userRepo.findById(dto.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ForumPost post = postRepo.findById(dto.postId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        ForumComment comment = new ForumComment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(dto.content());

        ForumComment saved = commentRepo.save(comment);

        // increment post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepo.save(post);

        return map(saved);
    }

    @Override
    public List<CommentResponse> getCommentsForPost(Long postId) {

        ForumPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return commentRepo.findByPost(post)
                .stream()
                .filter(c -> c.getStatus() == ForumComment.Status.ACTIVE)
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional
    public CommentResponse likeComment(Long commentId) {
        ForumComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        c.setLikeCount(c.getLikeCount() + 1);
        return map(commentRepo.save(c));
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        ForumComment c = commentRepo.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        c.setStatus( ForumComment.Status.DELETED);
        commentRepo.save(c);

        ForumPost post = c.getPost();
        post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        postRepo.save(post);
    }
}

