package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface NewsService {

    Page<News> getAllNews(int page, int size);

    News getNewsById(Long id);

    News createNews(News news);

    News createNewsWithImage(News news, MultipartFile file);
}
