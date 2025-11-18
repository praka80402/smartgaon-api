package com.smartgaon.ai.smartgaon_api.GaonConnectForum.Controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumcomment.CommentResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumCommentService;

import java.util.List;

@RestController
@RequestMapping("/api/forum/comments")
@CrossOrigin(origins = "*")
public class ForumCommentController {

    @Autowired
    private ForumCommentService commentService;

    @PostMapping
    public CommentResponse addComment(@RequestBody CommentCreateDto dto) {
        return commentService.addComment(dto);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getComments(@PathVariable Long postId) {
        return commentService.getCommentsForPost(postId);
    }

    @PostMapping("/{commentId}/like")
    public CommentResponse likeComment(@PathVariable Long commentId) {
        return commentService.likeComment(commentId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }
}
