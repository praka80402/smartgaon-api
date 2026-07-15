package com.smartgaon.ai.smartgaon_api.latestvillage.sg;


import java.time.Instant;
import java.util.List;

public class VillageRequestDtos {
    
    public static class SubmitData {
        public String name;
        public String district;
        public String state;
        public String description;
        public String submitterName;
        public String submitterPhone;
        public List<PlaceData> places;
    }

    public static class PlaceData {
        public String name;
        public String description;
        public String videoUrl;
        // photo comes as multipart file "placePhoto_{index}"
    }

    public static class PlaceResponse {
        public Long id;
        public String name;
        public String description;
        public String photo;
        public String videoUrl;

        public static PlaceResponse of(VillageRequestPlace p) {
            PlaceResponse r = new PlaceResponse();
            r.id = p.getId();
            r.name = p.getName();
            r.description = p.getDescription();
            r.photo = p.getPhoto();
            r.videoUrl = p.getVideoUrl();
            return r;
        }
    }

    public static class RequestResponse {
        public Long id;
        public String name;
        public String district;
        public String state;
        public String description;
        public String submitterName;
        public String submitterPhone;
        public List<String> images;
        public List<PlaceResponse> places;
        public String status;
        public String rejectionReason;
        public Long createdVillageId;
        public Instant createdAt;
        public Instant reviewedAt;

        public static RequestResponse of(VillageRequest v) {
            RequestResponse r = new RequestResponse();
            r.id = v.getId();
            r.name = v.getName();
            r.district = v.getDistrict();
            r.state = v.getState();
            r.description = v.getDescription();
            r.submitterName = v.getSubmitterName();
            r.submitterPhone = v.getSubmitterPhone();
            r.images = v.getImages();
            r.places = v.getPlaces().stream().map(PlaceResponse::of).toList();
            r.status = v.getStatus().name();
            r.rejectionReason = v.getRejectionReason();
            r.createdVillageId = v.getCreatedVillageId();
            r.createdAt = v.getCreatedAt();
            r.reviewedAt = v.getReviewedAt();
            return r;
        }
    }

    public static class RejectBody {
        public String reason;
    }
}
