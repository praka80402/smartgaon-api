package com.smartgaon.ai.smartgaon_api.successstory.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartgaon.ai.smartgaon_api.successstory.model.SuccessStory;
import com.smartgaon.ai.smartgaon_api.successstory.service.SuccessStoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/public/success-stories")
@RequiredArgsConstructor
public class SuccessStoryController {

    private final SuccessStoryService successStoryService;

    // USER – GET ALL PUBLISHED STORIES
    @GetMapping
    public List<SuccessStory> getAllSuccessStories() {
        return successStoryService.getAllPublished();
    }
    
    @GetMapping("/{id}")
    public SuccessStory getStoryById(@PathVariable Long id) {
        return successStoryService.getById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Success story not found"
                        )
                );
    }
    
    
}

