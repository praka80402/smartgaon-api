package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.NewsRepository;

import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final S3Service s3Service;

    // ✅ Paginated list
    @Override
    public Page<News> getAllNews(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("publishedAt").descending()
        );
        return newsRepository.findAll(pageable);
    }

    // ✅ Get by ID
    @Override
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
    }

    // ✅ Create without media
    @Override
    public News createNews(News news) {
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }

        if (news.getImageUrls() == null) {
            news.setImageUrls(List.of());
        }

        return newsRepository.save(news);
    }

    // ✅ Create with image (ALIGNED WITH ADMIN)
    @Override
    public News createNewsWithImage(News news, MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                String cloudUrl = s3Service.uploadFile(file);
                news.setImageUrls(List.of(cloudUrl));   // ✅ FIX
            }

            if (news.getPublishedAt() == null) {
                news.setPublishedAt(LocalDateTime.now());
            }

            return newsRepository.save(news);

        } catch (Exception e) {
            throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }
}
