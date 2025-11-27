package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface EventService {

    EventResponse createEvent(EventRequest request);

    EventResponse createEventWithImage(
            String title,
            String description,
            String startDateTime,
            String endDateTime,
            String location,
            String contactInfo,
            MultipartFile image
    );

    Page<EventResponse> getEvents(int page, int size);

    EventResponse getEventById(UUID id);

    void deleteEvent(UUID id);
}
