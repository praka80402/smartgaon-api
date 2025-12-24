package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findAllByOrderByPublishedAtDesc(Pageable pageable);
}

