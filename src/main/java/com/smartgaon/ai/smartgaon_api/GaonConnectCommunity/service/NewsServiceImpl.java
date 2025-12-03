package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.NewsRepository;
import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;
    private final CloudinaryService cloudinaryService;

    // ✅ Paginated list of news (for infinite scroll)
    @Override
    public Page<News> getAllNews(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("publishedAt").descending()
        );
        return newsRepository.findAllByOrderByPublishedAtDesc(pageable);
    }

    // ✅ Single news by id
    @Override
    public News getNewsById(Long id) {
        return newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
    }

    // ✅ Create news
    @Override
    public News createNews(News news) {
        if (news.getPublishedAt() == null) {
            news.setPublishedAt(LocalDateTime.now());
        }
        return newsRepository.save(news);
    }
    @Override
    public News createNewsWithImage(News news, MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                // Upload to Cloudinary
                String cloudUrl = cloudinaryService.uploadFile(file);

                // Set URL in entity
                news.setThumbnailUrl(cloudUrl);
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
