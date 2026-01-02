package com.smartgaon.ai.smartgaon_api.s3;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

public class TestRedisConnection {
    public static void main(String[] args) {

        String redisUrl = "redis://127.0.0.1:6379";

        System.out.println("🔄 Testing Redis without password...");

        try {
            RedisClient client = RedisClient.create(redisUrl);
            var connection = client.connect();
            RedisCommands<String, String> sync = connection.sync();

            sync.set("no-pass-test", "OK");
            System.out.println("VALUE = " + sync.get("no-pass-test"));

            connection.close();
            client.shutdown();
        } catch (Exception e) {
            System.out.println("❌ Redis connection failed!");
            e.printStackTrace();
        }
    }
}
