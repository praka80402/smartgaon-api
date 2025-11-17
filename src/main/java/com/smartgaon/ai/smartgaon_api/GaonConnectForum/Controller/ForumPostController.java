package com.smartgaon.ai.smartgaon_api.GaonConnectForum.Controller;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostCreateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.dto.forumpost.ForumPostUpdateDto;
import com.smartgaon.ai.smartgaon_api.GaonConnectForum.service.ForumPostService;

@RestController
@RequestMapping("/api/forum/posts")
public class ForumPostController {

    @Autowired
    private ForumPostService postService;

    @PostMapping
    public ForumPostResponse create(@RequestBody ForumPostCreateDto dto) {
        return postService.create(dto);
    }

    @GetMapping("/{id}")
    public ForumPostResponse getById(@PathVariable Long id) {
        return postService.getById(id);
    }

    @GetMapping
    public Page<ForumPostResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postService.list(pageable);
    }

    @GetMapping("/search")
    public Page<ForumPostResponse> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.search(query, pageable);
    }

    @PutMapping("/{id}")
    public ForumPostResponse update(@PathVariable Long id, @RequestBody ForumPostUpdateDto dto) {
        return postService.update(id, dto);
    }

    @PostMapping("/{id}/like")
    public ForumPostResponse like(@PathVariable Long id) {
        return postService.like(id);
    }

    @PostMapping("/{id}/comment-count")
    public ForumPostResponse incrementComment(@PathVariable Long id) {
        return postService.incrementCommentCount(id);
    }

    @PostMapping("/{id}/moderate")
    public ForumPostResponse moderate(@PathVariable Long id, @RequestParam String status) {
        return postService.moderate(id, status);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        postService.delete(id);
    }
}
