package com.smartgaon.ai.smartgaon_api.chatbot;



import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*")
public class ChatbotController {

    private final List<Map<String, String>> faqList = new ArrayList<>();

    public ChatbotController() {
        loadFAQ();
    }

    private void loadFAQ() {
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("chatbotquestion.csv").getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            String line = reader.readLine(); 

            while ((line = reader.readLine()) != null) {
                String[] qa = line.split(",", 2); 
                if (qa.length == 2) {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("question", qa[0].trim());
                    entry.put("answer", qa[1].trim().replaceAll("^\"|\"$", ""));
                    faqList.add(entry);
                }
            }

            reader.close();
            System.out.println("✅ FAQ Loaded Successfully. Total FAQs: " + faqList.size());

        } catch (Exception e) {
            System.out.println("❌ Error loading  " + e.getMessage());
        }
    }

    @PostMapping
    public Map<String, String> getAnswer(@RequestBody Map<String, String> body) {

        String userQuestion = body.getOrDefault("message", "").toLowerCase().trim();
        String reply = "⚠️ Sorry, I don't understand that question. Please try asking differently.";

        LevenshteinDistance ld = new LevenshteinDistance();
        double bestMatch = 0.0;

        for (Map<String, String> qa : faqList) {
            String storedQuestion = qa.get("question").toLowerCase();

            // ✅ DIRECT SUBSTRING MATCH (Highest Priority)
            if (storedQuestion.contains(userQuestion) || userQuestion.contains(storedQuestion)) {
                return Collections.singletonMap("reply", qa.get("answer"));
            }

            // ✅ SIMILARITY MATCH
            int distance = ld.apply(userQuestion, storedQuestion);
            double maxLen = Math.max(userQuestion.length(), storedQuestion.length());
            double similarity = (1 - (double) distance / maxLen) * 100;

            if (similarity >= 40 && similarity > bestMatch) {
                bestMatch = similarity;
                reply = qa.get("answer");
            }
        }

        return Collections.singletonMap("reply", reply);
    }

    
}
