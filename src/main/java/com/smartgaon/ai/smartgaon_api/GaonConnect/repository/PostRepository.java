package com.smartgaon.ai.smartgaon_api.GaonConnect.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartgaon.ai.smartgaon_api.GaonConnect.model.Post;
public interface PostRepository extends JpaRepository<Post, UUID> {

}
