package com.smartgaon.ai.smartgaon_api.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
        if (language == null) language = "en";

        return switch (language.toLowerCase()) {
            case "hi", "hindi" -> """
                कृपया सभी प्रश्न, विकल्प और उत्तर *हिंदी* भाषा में दें।
                आउटपुट केवल JSON हो, कोई व्याख्या नहीं।

                """ + prompt;

            case "mr", "marathi" -> """
                कृपया सर्व प्रश्न, पर्याय व उत्तरे *मराठी* भाषेत द्या.
                फक्त JSON आउटपुट द्या, कोणतेही स्पष्टीकरण नाही.

                """ + prompt;

            default -> """
                Provide all questions and answers in English language only.

                """ + prompt;
        };
    }

    private String lastSixMonthsDateRange() {
        LocalDate today = LocalDate.now();
        LocalDate sixMonthsAgo = today.minusMonths(6);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        return "%s to %s".formatted(
                sixMonthsAgo.format(fmt),
                today.format(fmt)
        );
    }

    // ---------- GOVT EXAM DAILY QUIZ ----------
    public String generateGovtExamDailyQuiz(int count, String language) {

        String dateRange = lastSixMonthsDateRange(); // <-- DYNAMIC

        String prompt = """
        Generate %d multiple choice questions for Indian Government exam preparation.

        Exam types: SSC, Railway, Banking, UPSC, Police, Army.

        Topic distribution:
        - 30%% Static GK
        - 20%% Current Affairs (India) from %s
        - 20%% Reasoning (easy-medium)
        - 20%% Quantitative aptitude (basic maths)
        - 10%% English language

        Rules:
        - Provide output ONLY as JSON array
        - Each question must have 4 options + exact correct answer
        - No explanation, no markdown

        Format:
        [
          {"question": "...", "options": ["A","B","C","D"], "answer": "A"}
        ]
    """.formatted(count, dateRange);

        return callGroq(applyLanguage(prompt, language));
    }


    // ---------- CLASS + SUBJECT BASED QUESTIONS ----------
    public String generateQuestionsForClassAndSubject(
            int classGrade, String subject, int count, String language) {

        String prompt = """
            Generate %d multiple choice questions for students of Class %d.
            Subject: %s

            Rules:
            - Difficulty must match Class %d level
            - Provide only JSON array output
            - 4 options per question, correct answer text only

            Format:
            [
              {"question": "...", "options": ["A","B","C","D"], "answer": "A"}
            ]
        """.formatted(count, classGrade, subject, classGrade);

        return callGroq(applyLanguage(prompt, language));
    }

    // ---------- CATEGORY QUIZ ----------
    public String generateQuestions(String category, int count, String language) {
        String basePrompt = buildCategoryPrompt(category, count);
        String prompt = basePrompt + """

            Rules:
            - Provide output in JSON array only
            - 4 options per question
            - Correct answer text only
            - No explanation, no markdown

            Format:
            [
              {"question": "...", "options": ["A","B","C","D"], "answer": "A"}
            ]
        """;

        return callGroq(applyLanguage(prompt, language));
    }

    // ---------- SHARED GROQ CALL (Fix missing method) ----------
    private String callGroq(String prompt) {
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
        """.formatted(prompt.replace("\n", "\\n")
                .replace("\"", "\\\""));

        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + groqApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    // ---------- PARSE QUIZ JSON ----------
    public List<QuizQuestion> extractQuestions(String rawResponse) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<?,?> root = mapper.readValue(rawResponse, new TypeReference<>() {});
        String jsonContent = ((Map<?,?>)((Map<?,?>)((List<?>)root.get("choices")).get(0))
                .get("message")).get("content").toString();

        return mapper.readValue(jsonContent, new TypeReference<>() {});
    }

    // ---------- GENERATE + PARSE ----------
    public List<QuizQuestion> generateAndParseQuiz(String category, int count, String language) throws Exception {
        String response = generateQuestions(category, count, language);
        return extractQuestions(response);
    }
}
