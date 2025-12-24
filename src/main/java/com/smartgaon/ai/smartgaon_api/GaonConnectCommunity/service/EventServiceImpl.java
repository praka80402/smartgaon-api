//package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;
//
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventRequest;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventResponse;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Event;
//import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.EventRepository;
//import com.smartgaon.ai.smartgaon_api.cloudinary.CloudinaryService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.*;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class EventServiceImpl implements EventService {
//
//    private final EventRepository repo;
//    private final CloudinaryService cloudinaryService;
//
//    @Override
//    public EventResponse createEvent(EventRequest request) {
//
//        Event event = Event.builder()
//                .title(request.getTitle())
//                .description(request.getDescription())
//                .startDateTime(LocalDateTime.parse(request.getStartDateTime()))
//                .endDateTime(LocalDateTime.parse(request.getEndDateTime()))
//                .location(request.getLocation())
//                .contactInfo(request.getContactInfo())
//                .build();
//
//        repo.save(event);
//        return map(event);
//    }
//
//    @Override
//    public EventResponse createEventWithImage(
//            String title,
//            String description,
//            String startDateTime,
//            String endDateTime,
//            String location,
//            String contactInfo,
//            MultipartFile image
//    ) {
//        String url = cloudinaryService.uploadFile(image);
//
//        Event event = Event.builder()
//                .title(title)
//                .description(description)
//                .startDateTime(LocalDateTime.parse(startDateTime))
//                .endDateTime(LocalDateTime.parse(endDateTime))
//                .location(location)
//                .contactInfo(contactInfo)
//                .imageUrls(url)
//                .build();
//
//        repo.save(event);
//        return map(event);
//    }
//
//    @Override
//    public Page<EventResponse> getEvents(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        return repo.findAll(pageable).map(this::map);
//    }
//
//    @Override
//    public EventResponse getEventById(UUID id) {
//        return repo.findById(id)
//                .map(this::map)
//                .orElseThrow(() -> new RuntimeException("Event not found"));
//    }
//
//    @Override
//    public void deleteEvent(UUID id) {
//        repo.deleteById(id);
//    }
//
//    private EventResponse map(Event e) {
//        return EventResponse.builder()
//                .id(e.getId())
//                .title(e.getTitle())
//                .description(e.getDescription())
//                .startDateTime(e.getStartDateTime())
//                .endDateTime(e.getEndDateTime())
//                .location(e.getLocation())
//                .contactInfo(e.getContactInfo())
//                .pictureUrl(e.getPictureUrl())
//                .createdAt(e.getCreatedAt())
//                .build();
//    }
//}

package com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.service;

import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventRequest;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.dto.EventResponse;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.model.Event;
import com.smartgaon.ai.smartgaon_api.GaonConnectCommunity.repository.EventRepository;
import com.smartgaon.ai.smartgaon_api.s3.S3Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository repo;
    private final S3Service s3Service;

    @Override
    public EventResponse createEvent(EventRequest request) {

        Event event = Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startDateTime(LocalDateTime.parse(request.getStartDateTime()))
                .endDateTime(LocalDateTime.parse(request.getEndDateTime()))
                .location(request.getLocation())
                .contactInfo(request.getContactInfo())
                .build();

        repo.save(event);
        return map(event);
    }

    @Override
    public EventResponse createEventWithImage(
            String title,
            String description,
            String startDateTime,
            String endDateTime,
            String location,
            String contactInfo,
            MultipartFile image
    ) {
        String url = s3Service.uploadFile(image);

        Event event = Event.builder()
                .title(title)
                .description(description)
                .startDateTime(LocalDateTime.parse(startDateTime))
                .endDateTime(LocalDateTime.parse(endDateTime))
                .location(location)
                .contactInfo(contactInfo)
                .imageUrls(List.of(url))   // ✅ FIX
                .build();

        repo.save(event);
        return map(event);
    }

    @Override
    public Page<EventResponse> getEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return repo.findAll(pageable).map(this::map);
    }

    @Override
    public EventResponse getEventById(UUID id) {
        return repo.findById(id)
                .map(this::map)
                .orElseThrow(() -> new RuntimeException("Event not found"));
    }

    @Override
    public void deleteEvent(UUID id) {
        repo.deleteById(id);
    }

    private EventResponse map(Event e) {
        return EventResponse.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .startDateTime(e.getStartDateTime())
                .endDateTime(e.getEndDateTime())
                .location(e.getLocation())
                .contactInfo(e.getContactInfo())
                .imageUrls(e.getImageUrls())   // ✅ FIX
                .createdAt(e.getCreatedAt())
                .build();
    }
}

