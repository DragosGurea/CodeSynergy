package com.groupama.service;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PubSubService {

    static String projectId = "prj-hackathon-team4";
    static String topicName = "projects/prj-hackathon-team4/topics/document-ai-processor";
    static Logger  logger = Logger.getLogger(PubSubService.class.getName());

    public static void publishVertexAiResult(String message) {

        ByteString byteStr = ByteString.copyFrom(message, StandardCharsets.UTF_8);
        PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();

        Publisher publisher;

        try {
            publisher = Publisher.newBuilder(
                    ProjectTopicName.parse(topicName)).build();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error creating publisher with topic : " + topicName, e);
            throw new RuntimeException(e);
        }

        try {
            String messageId = publisher.publish(pubsubApiMessage).get();
            logger.log(Level.INFO, "Published message Id: " + messageId);
        } catch (InterruptedException | ExecutionException e) {
            logger.log(Level.SEVERE, "Error publishing Pub/Sub message: " + e.getMessage(), e);
        } finally {
            publisher.shutdown();
        }
    }
}
