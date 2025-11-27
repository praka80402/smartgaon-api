package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.controller;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/news")

@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;
    
    @PostMapping
    public ResponseEntity<?> createNews(@RequestBody News news) {
        return ResponseEntity.ok(newsService.createNews(news));
    }


    @GetMapping
    public ResponseEntity<?> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(newsService.getAllNews(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }
}
