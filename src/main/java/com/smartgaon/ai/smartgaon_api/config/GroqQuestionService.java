package com.smartgaon.ai.smartgaon_api.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
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
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${groq.api.key}")
    private String groqApiKey;

    public GroqQuestionService(WebClient webClient) {
        this.webClient = webClient;
    }

    // ---------- CATEGORY-BASED PROMPT BUILDER ----------
    private String buildCategoryPrompt(String category, int count) {
        return switch (category.toLowerCase()) {

            case "citizen" -> """
            Generate %d multiple choice quiz questions for Indian citizens.
            Topics: constitution, rights & duties, public services, government schemes, traffic safety,
            environment, digital literacy, civic responsibility.
        """.formatted(count);

            case "farmer" -> """
            Generate %d multiple choice quiz questions for farmers.
            Topics: crops, fertilizers, pesticides, soil health, irrigation, organic farming,
            government agriculture schemes like PM-KISAN, MSP, KCC.
        """.formatted(count);

            case "vendor" -> """
            Generate %d multiple choice quiz questions for street vendors & small shop owners.
            Topics: GST basics, business license, digital payments (UPI), customer safety,
            PM SVANidhi scheme, basic accounting, market practices.
        """.formatted(count);

            case "teacher" -> """
            Generate %d multiple choice quiz questions for school teachers.
            Topics: pedagogy, NEP 2020, child psychology, learning outcomes, classroom management,
            teaching methods.
        """.formatted(count);

            case "electrician" -> """
            Generate %d multiple choice quiz questions for electricians.
            Topics: wiring safety, electrical tools, circuits, voltage/current basics,
            household electrical systems, government safety standards, renewable energy basics.
        """.formatted(count);

            case "doctor" -> """
            Generate %d multiple choice quiz questions for doctors and medical students.
            Topics: anatomy, diseases, first aid, public health programs, medicines, nutrition.
        """.formatted(count);

            case "student" -> """
            Generate %d multiple choice quiz questions for students.
            Topics: science, maths, history, general knowledge, environment.
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
    public String generateGovtExamDailyQuiz(
            int count,
            String language,
            String examType   // <-- NOW ONLY ONE
    ) {

        String dateRange = lastSixMonthsDateRange();

        String prompt = """
        Generate %d multiple choice questions for Indian Government exam preparation.

        Exam type: %s

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
        """
                .formatted(count, examType, dateRange);

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


    public CareerGuideResponse generateCareerGuide(
            String careerField,
            int yearsExperience,
            String language
    ) throws Exception {

        String prompt = buildPrompt(careerField, yearsExperience, language);
        String rawResponse = callGroq(prompt);

        // 1️⃣ parse Groq wrapper
        Map<String,Object> root = mapper.readValue(rawResponse, new TypeReference<>() {});
        Map<String,Object> choice0 = (Map<String,Object>) ((List<?>) root.get("choices")).get(0);
        Map<String,Object> message = (Map<String,Object>) choice0.get("message");
        String content = (String) message.get("content");

        // 2️⃣ strip code fences
        content = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // 3️⃣ extract only JSON object between FIRST { and LAST }
        int start = content.indexOf("{");
        int end   = content.lastIndexOf("}") + 1;
        String justJson = content.substring(start, end);


        // 4️⃣ parse into your POJO
        return mapper.readValue(justJson, CareerGuideResponse.class);
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

    /** ---------- PROMPT ---------- **/
    private String buildPrompt(String career, int exp, String language) {

        String langHeader = switch (language.toLowerCase()) {
            case "hi", "hindi" -> "Provide answers in *Hindi*.";
            case "mr", "marathi" -> "Answer in *Marathi*.";
            default -> "Answer in *English*.";
        };

        return """
            %s

            Create a detailed *career roadmap* for someone interested in: **%s**
            Current experience level: **%d years**

            Include:
            - Recommended learning path (step-by-step)
            - Skills required (beginner -> advanced)
            - Certifications / courses (free + paid)
            - Job roles to target at each stage
            - Expected salary range in India (approx.)
            - Tools & technologies needed
            - Mistakes to avoid
            - First 30-day action plan

            STRICT RULES:
            - Output must be JSON ONLY
            - No markdown, no extra text, no explanation outside JSON
            - Structure must match:

            {
              "field": "",
              "summary": "",
              "skills": {
                "beginner": [],
                "intermediate": [],
                "advanced": []
              },
              "roadmap": [
                {"stage": "", "description": "", "duration": "x months"}
              ],
              "certifications": [
                {"name": "", "provider": "", "free": true}
              ],
              "job_roles": [],
              "salary_india": {
                "entry": "",
                "mid": "",
                "senior": ""
              },
              "action_plan_30_days": [
                "Day 1-7: ...",
                "Day 8-15: ..."
              ],
              "common_mistakes": []
            }
        """.formatted(langHeader, career, exp);
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
    /** ---------- RESPONSE MODELS ---------- **/
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CareerGuideResponse {
        public String field;
        public String summary;
        public Skills skills;
        public List<RoadmapStep> roadmap;
        public Salary salary_india;
        public List<String> job_roles;
        public List<String> action_plan_30_days;
        public List<Certification> certifications;
        public List<String> common_mistakes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Skills {
        private List<String> beginner;
        private List<String> intermediate;
        private List<String> advanced;
    }

    @Data
    public static class RoadmapStep {
        private String stage;
        private String description;
        private String duration;
    }

    @Data
    public static class Certification {
        private String name;
        private String provider;
        private boolean free;
    }

    @Data
    public static class Salary {
        private String entry;
        private String mid;
        private String senior;
    }


}
