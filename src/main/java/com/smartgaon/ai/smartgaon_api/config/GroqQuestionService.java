package com.smartgaon.ai.smartgaon_api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqQuestionService {

    private final WebClient webClient;

    @Value("${groq.api.key}")
    private String groqApiKey;

    public GroqQuestionService(WebClient webClient) {
        this.webClient = webClient;
    }

    // ---------- CATEGORY-BASED PROMPT BUILDER ----------
    private String buildCategoryPrompt(String category, int count) {
        return switch (category.toLowerCase()) {
            case "teacher" -> """
                Generate %d multiple choice quiz questions for school teachers.
                Topics: pedagogy, education, classroom management, learning psychology, teaching skills.
            """.formatted(count);

            case "farmer" -> """
                Generate %d multiple choice quiz questions for farmers.
                Topics: crops, fertilizers, pesticides, soil, irrigation, organic farming.
            """.formatted(count);

            case "doctor" -> """
                Generate %d multiple choice quiz questions for doctors and medical students.
                Topics: anatomy, diseases, first aid, medicines, healthcare basics.
            """.formatted(count);

            case "student" -> """
                Generate %d multiple choice quiz questions for students.
                Topics: general science, maths, social studies, environment.
            """.formatted(count);

            default -> """
                Generate %d general knowledge multiple choice quiz questions.
            """.formatted(count);
        };
    }

    // ---------- LANGUAGE FORMATTER ----------
    private String applyLanguage(String prompt, String language) {
        if (language == null || language.isBlank())
            return "Please give the output in English.\n" + prompt;

        return switch (language.toLowerCase()) {
            case "hi", "hindi" -> """
                कृपया सभी प्रश्न, विकल्प और उत्तर *हिंदी* भाषा में दें।
                आउटपुट केवल JSON हो, कोई व्याख्या नहीं।
                
                """ + prompt;

            case "mr", "marathi" -> """
                कृपया सर्व प्रश्न, पर्याय व उत्तरे *मराठी* भाषेत द्या.
                फक्त JSON आउटपुट द्या, कोणतेही स्पष्टीकरण नाही.
                
                """ + prompt;

            case "en", "english" -> """
                Please give all questions and options in English language.
                """ + prompt;

            default -> """
                Please give all questions and options in English language.
                """ + prompt;
        };
    }

    // ----------------- Call Groq -----------------
    public String generateQuestions(String category, int count, String language) {

        String basePrompt = buildCategoryPrompt(category, count);
        String prompt = basePrompt + """
        
        Rules:
        - Provide output in valid JSON array only
        - 4 options per question
        - specify exact correct answer as text
        - no explanation, no markdown
        Format:
        [
          {
            "question": "....",
            "options": ["A","B","C","D"],
            "answer": "A"
          }
        ]
        """;

        // 🟢 Apply Hindi / Marathi / English
        prompt = applyLanguage(prompt, language);

        String requestBody = """
        {
          "model": "llama-3.3-70b-versatile",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ],
          "temperature": 0.7
        }
        """.formatted(prompt.replace("\n","\\n").replace("\"","\\\""));

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ----------------- Parse JSON from Groq -----------------
    public List<QuizQuestion> extractQuestions(String rawResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<?,?> root = mapper.readValue(rawResponse, new TypeReference<Map<String,Object>>(){});
        String jsonContent = ((Map<?,?>)((Map<?,?>)((List<?>)root.get("choices")).get(0))
                .get("message")).get("content").toString();

        return mapper.readValue(jsonContent, new TypeReference<List<QuizQuestion>>() {});
    }

    // ----------------- Generate & Parse in one step -----------------
    public List<QuizQuestion> generateAndParseQuiz(String category, int count, String language) throws Exception {
        String response = generateQuestions(category, count, language);
        return extractQuestions(response);
    }
}
