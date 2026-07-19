package com.smartgaon.ai.smartgaon_api.latestvillage.sg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * PUBLIC side of village requests.
 *
 * Images are uploaded to S3 (the bucket can stay fully PRIVATE — no public
 * bucket policy needed). The URL stored in the DB points back at THIS
 * backend's /files endpoint, which streams the object from S3 using the
 * same credentials it uploads with.
 *
 * application.properties:
 *   aws.s3.images-bucket=smartgaon
 *   aws.region=ap-south-1            (already present)
 *   aws.accessKeyId=...              (already present)
 *   aws.secretAccessKey=...          (already present)
 *   app.files.base-url=http://localhost:8080   ← this backend's own address
 *                                                (production: https://smartgaon.duckdns.org)
 */
@Service
public class VillageRequestSubmitService {

    private final VillageRequestRepository requestRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String baseUrl;
    private volatile S3Client s3; // created lazily

    public VillageRequestSubmitService(VillageRequestRepository requestRepo,
                                       @Value("${aws.s3.images-bucket:smartgaon}") String bucket,
                                       @Value("${aws.region:ap-south-1}") String region,
                                       @Value("${aws.accessKeyId:}") String accessKey,
                                       @Value("${aws.secretAccessKey:}") String secretKey,
                                       @Value("${app.files.base-url:https://smartgaon.duckdns.org}") String baseUrl) {
        this.requestRepo = requestRepo;
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.baseUrl = baseUrl.replaceAll("/$", "");
    }

    private S3Client s3() {
        S3Client c = s3;
        if (c == null) {
            synchronized (this) {
                if (s3 == null) {
                    var builder = S3Client.builder().region(Region.of(region));
                    if (accessKey != null && !accessKey.isBlank()) {
                        builder = builder.credentialsProvider(
                                StaticCredentialsProvider.create(
                                        AwsBasicCredentials.create(accessKey, secretKey)));
                    }
                    s3 = builder.build();
                }
                c = s3;
            }
        }
        return c;
    }

    /*
       Multipart request:
       - "data"          : JSON string (SubmitData)
       - "images"        : village image files (0..n)
       - "placePhoto_0"… : one photo per place, index matches data.places
    */
    @Transactional
    public VillageRequestDtos.RequestResponse submit(MultipartHttpServletRequest req) throws Exception {
        String json = req.getParameter("data");
        if (json == null || json.isBlank()) throw new IllegalArgumentException("Missing form data");
        VillageRequestDtos.SubmitData data = mapper.readValue(json, VillageRequestDtos.SubmitData.class);

        if (data.name == null || data.name.isBlank())
            throw new IllegalArgumentException("Village name is required");

        // ── DEBUG: shows exactly what the browser sent ──
        System.out.println("[village-request] village images received: "
                + req.getFiles("images").size()
                + " | multipart file field names: " + req.getFileMap().keySet()
                + " | content-type: " + req.getContentType());

        VillageRequest vr = new VillageRequest();
        vr.setName(data.name.trim());
        vr.setDistrict(data.district);
        vr.setState(data.state);
        vr.setDescription(data.description);
        vr.setSubmitterName(data.submitterName);
        vr.setSubmitterPhone(data.submitterPhone);

        // village images → uploaded to S3, proxy URLs stored in DB
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile f : req.getFiles("images")) {
            String url = uploadToS3(f, "village-requests/images");
            if (url != null) imageUrls.add(url);
        }
        vr.setImages(imageUrls);

        // popular places
        if (data.places != null) {
            for (int i = 0; i < data.places.size(); i++) {
                VillageRequestDtos.PlaceData pd = data.places.get(i);
                if (pd.name == null || pd.name.isBlank()) continue;
                VillageRequestPlace p = new VillageRequestPlace();
                p.setName(pd.name.trim());
                p.setDescription(pd.description);
                p.setVideoUrl(pd.videoUrl);
                MultipartFile photo = req.getFile("placePhoto_" + i);
                if (photo != null && !photo.isEmpty())
                    p.setPhoto(uploadToS3(photo, "village-requests/places"));
                p.setRequest(vr);
                vr.getPlaces().add(p);
            }
        }

        return VillageRequestDtos.RequestResponse.of(requestRepo.save(vr));
    }

    /** Streams an object from S3 for the controller's /files endpoint. */
    public ResponseBytes<GetObjectResponse> readObject(String key) {
        if (key.contains("..")) throw new IllegalArgumentException("Invalid key");
        return s3().getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucket).key(key).build());
    }

    /* ── S3 upload ── */
    private String uploadToS3(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot).toLowerCase();
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)"))
            throw new IllegalArgumentException("File type not allowed: " + ext);

        String key = folder + "/" + UUID.randomUUID().toString().replace("-", "") + ext;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3().putObject(put, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // URL points at THIS backend, which streams the object from the
        // private bucket — no public bucket policy required.
        return baseUrl + "/api/public/village-requests/files/" + key;
    }
}