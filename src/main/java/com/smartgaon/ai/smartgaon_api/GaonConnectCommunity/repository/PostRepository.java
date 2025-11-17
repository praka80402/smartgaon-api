package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Post;
public interface PostRepository extends JpaRepository<Post, UUID> {

}
