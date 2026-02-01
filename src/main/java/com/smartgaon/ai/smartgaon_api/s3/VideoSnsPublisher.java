package com.smartgaon.ai.smartgaon_api.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Service
@RequiredArgsConstructor
public class VideoSnsPublisher {

    private final SnsClient snsClient;

    @Value("${aws.sns.video-topic-arn}")
    private String topicArn;

    public void publishVideoUploadedEvent(String message) {
        snsClient.publish(
                PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(message)
                        .build()
        );
    }

    public void publishVideoProcessingEvent(
            Long entryId,
            String rawVideoUrl,
            String category
    ) {
        String message = """
        {
          "entryId": %d,
          "rawVideoUrl": "%s",
          "category": "%s"
        }
        """.formatted(entryId, rawVideoUrl, category);

        snsClient.publish(
                PublishRequest.builder()
                        .topicArn(topicArn)
                        .message(message)
                        .build()
        );
    }
}
