package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

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
}
