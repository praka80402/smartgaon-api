package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
}
