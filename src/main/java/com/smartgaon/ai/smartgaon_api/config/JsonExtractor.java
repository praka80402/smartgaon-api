package com.smartgaon.ai.smartgaon_api.config;

public class JsonExtractor {

    public static String extractJson(String text) {
        int lastOpen = text.lastIndexOf('{');
        int braceCount = 0;

        for (int i = lastOpen; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '{') braceCount++;
            else if (c == '}') braceCount--;

            if (braceCount == 0) {
                return text.substring(lastOpen, i + 1);
            }
        }

        return null; // no JSON found
    }
}
