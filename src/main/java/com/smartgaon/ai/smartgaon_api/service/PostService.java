package com.smartgaon.ai.smartgaon_api.service;

import com.smartgaon.ai.smartgaon_api.model.Post;
import com.smartgaon.ai.smartgaon_api.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PostService {

    @Autowired
    private PostRepository repo;

    public Post createPost(Post post) {
        return repo.save(post);
    }

    public List<Post> getAllPosts() {
        return repo.findAllByOrderByCreatedAtDesc();
    }
}