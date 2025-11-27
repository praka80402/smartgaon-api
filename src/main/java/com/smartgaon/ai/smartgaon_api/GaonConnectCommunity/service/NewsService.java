package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import org.springframework.data.domain.Page;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;

public interface NewsService {
    Page<News> getAllNews(int page, int size);

    News getNewsById(Long id);

    News createNews(News news);
}