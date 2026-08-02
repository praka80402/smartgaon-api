package com.smartgaon.ai.smartgaon_api.doctor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PublicGaonDoctorService {

    private final DoctorRepository doctorRepository;

    // ---------- API models ----------

    public record Option(String value, String label, int severity) {}
    public record Question(String id, String text, List<Option> options) {}

    @Data
    @AllArgsConstructor
    public static class QuestionResponse {
        private String category;
        private int questionNumber;   // 1..5
        private int totalQuestions;   // always 5
        private Question question;
    }

    public record Answer(String questionId, String value) {}

    @Data
    @AllArgsConstructor
    public static class TreatmentResult {
        private String category;
        private String severity;      // "mild" | "moderate" | "urgent"
        private String treatmentAdvice;
        private String consultNote;
        private String recommendedSpecialty;
        private List<Doctor> doctors;
    }

    // ---------- category knowledge base ----------

    private static final String GENERAL_PHYSICIAN = "GENERAL_PHYSICIAN";
    private static final String DERMATOLOGIST = "DERMATOLOGIST";
    private static final String ORTHOPEDIC = "ORTHOPEDIC";
    private static final String ENT = "ENT";
    private static final String GASTROENTEROLOGIST = "GASTROENTEROLOGIST";

    private static final Map<String, List<String>> CATEGORY_KEYWORDS = new LinkedHashMap<>();
    private static final Map<String, List<Question>> CATEGORY_QUESTIONS = new LinkedHashMap<>();
    private static final Map<String, String> CATEGORY_SPECIALTY = new HashMap<>();
    private static final Map<String, String> CATEGORY_LABEL = new HashMap<>();

    static {
        CATEGORY_KEYWORDS.put("FEVER", List.of("fever", "bukhar", "temperature", "chills"));
        CATEGORY_KEYWORDS.put("COUGH_COLD", List.of("cough", "cold", "khansi", "sardi", "throat", "sneeze"));
        CATEGORY_KEYWORDS.put("STOMACH", List.of("stomach", "pet", "diarrhea", "vomit", "loose motion", "nausea"));
        CATEGORY_KEYWORDS.put("HEADACHE", List.of("headache", "head pain", "sar dard", "migraine"));
        CATEGORY_KEYWORDS.put("BODY_PAIN", List.of("body pain", "joint", "back pain", "muscle", "knee"));
        CATEGORY_KEYWORDS.put("SKIN", List.of("rash", "itch", "skin", "allergy", "khujli"));

        CATEGORY_SPECIALTY.put("FEVER", GENERAL_PHYSICIAN);
        CATEGORY_SPECIALTY.put("COUGH_COLD", ENT);
        CATEGORY_SPECIALTY.put("STOMACH", GASTROENTEROLOGIST);
        CATEGORY_SPECIALTY.put("HEADACHE", GENERAL_PHYSICIAN);
        CATEGORY_SPECIALTY.put("BODY_PAIN", ORTHOPEDIC);
        CATEGORY_SPECIALTY.put("SKIN", DERMATOLOGIST);
        CATEGORY_SPECIALTY.put("GENERAL", GENERAL_PHYSICIAN);

        CATEGORY_LABEL.put("FEVER", "Fever");
        CATEGORY_LABEL.put("COUGH_COLD", "Cough & Cold");
        CATEGORY_LABEL.put("STOMACH", "Stomach Trouble");
        CATEGORY_LABEL.put("HEADACHE", "Headache");
        CATEGORY_LABEL.put("BODY_PAIN", "Body / Joint Pain");
        CATEGORY_LABEL.put("SKIN", "Skin Problem");
        CATEGORY_LABEL.put("GENERAL", "General Health");

        CATEGORY_QUESTIONS.put("FEVER", List.of(
            new Question("q1", "How long have you had the fever?", List.of(
                new Option("lt1", "Less than 1 day", 0),
                new Option("d1_3", "1-3 days", 1),
                new Option("gt3", "More than 3 days", 2))),
            new Question("q2", "Is the fever high (feels very hot / above 102°F)?", List.of(
                new Option("no", "No, mild", 0),
                new Option("yes", "Yes, quite high", 2))),
            new Question("q3", "Do you have chills, body ache, or weakness with it?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Any breathing difficulty, chest pain, or persistent vomiting?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Is this for a child under 5, a pregnant woman, or an elderly person?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1)))
        ));

        CATEGORY_QUESTIONS.put("COUGH_COLD", List.of(
            new Question("q1", "How long has the cough/cold lasted?", List.of(
                new Option("lt3", "Less than 3 days", 0),
                new Option("d3_7", "3-7 days", 1),
                new Option("gt7", "More than a week", 2))),
            new Question("q2", "Is there a sore throat or blocked nose?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 0))),
            new Question("q3", "Any fever along with the cough?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Any breathlessness, wheezing, or chest tightness?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Is the cough dry, or with thick/coloured phlegm?", List.of(
                new Option("dry", "Dry cough", 0),
                new Option("phlegm", "With phlegm", 1)))
        ));

        CATEGORY_QUESTIONS.put("STOMACH", List.of(
            new Question("q1", "Is it stomach pain, loose motions, vomiting, or all of these?", List.of(
                new Option("pain", "Pain only", 0),
                new Option("loose", "Loose motions", 1),
                new Option("vomit", "Vomiting", 1),
                new Option("all", "More than one", 2))),
            new Question("q2", "How many times has this happened today?", List.of(
                new Option("1_2", "1-2 times", 0),
                new Option("3_5", "3-5 times", 1),
                new Option("gt5", "More than 5 times", 2))),
            new Question("q3", "Any blood in stool/vomit, or severe pain?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q4", "Any signs of weakness, dizziness, or very little urination (dehydration)?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Did this start after any particular food, or is a child affected?", List.of(
                new Option("no", "No / not sure", 0),
                new Option("yes", "Yes", 1)))
        ));

        CATEGORY_QUESTIONS.put("HEADACHE", List.of(
            new Question("q1", "How long has the headache lasted?", List.of(
                new Option("lt1", "A few hours", 0),
                new Option("d1_2", "1-2 days", 1),
                new Option("gt2", "More than 2 days", 2))),
            new Question("q2", "Is it a mild dull ache or a severe throbbing pain?", List.of(
                new Option("mild", "Mild", 0),
                new Option("severe", "Severe", 2))),
            new Question("q3", "Any nausea, sensitivity to light, or blurred vision?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Any fever, neck stiffness, or recent head injury?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Do headaches like this happen often (recurring)?", List.of(
                new Option("no", "First time / rare", 0),
                new Option("yes", "Yes, recurring", 1)))
        ));

        CATEGORY_QUESTIONS.put("BODY_PAIN", List.of(
            new Question("q1", "Where is the pain mainly - joints, back, or all over the body?", List.of(
                new Option("joint", "Joints", 1),
                new Option("back", "Back", 1),
                new Option("all", "All over the body", 1))),
            new Question("q2", "How long have you had this pain?", List.of(
                new Option("lt3", "Less than 3 days", 0),
                new Option("gt3", "More than 3 days", 1))),
            new Question("q3", "Was there a recent injury, fall, or heavy lifting?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Is there swelling, redness, or difficulty moving the area?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Any fever along with the body pain?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1)))
        ));

        CATEGORY_QUESTIONS.put("SKIN", List.of(
            new Question("q1", "Is it itching, a rash, or a wound/injury on the skin?", List.of(
                new Option("itch", "Itching", 0),
                new Option("rash", "Rash", 1),
                new Option("wound", "Wound/injury", 1))),
            new Question("q2", "How long has it been there?", List.of(
                new Option("lt3", "Less than 3 days", 0),
                new Option("gt3", "More than 3 days", 1))),
            new Question("q3", "Is it spreading to other parts of the body?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Any pus, foul smell, fever, or severe swelling?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Did it start after a new food, soap, plant, or insect bite?", List.of(
                new Option("no", "No / not sure", 0),
                new Option("yes", "Yes", 1)))
        ));

        CATEGORY_QUESTIONS.put("GENERAL", List.of(
            new Question("q1", "How long have you had this problem?", List.of(
                new Option("lt3", "Less than 3 days", 0),
                new Option("gt3", "More than 3 days", 1))),
            new Question("q2", "Would you say the discomfort is mild, moderate, or severe?", List.of(
                new Option("mild", "Mild", 0),
                new Option("moderate", "Moderate", 1),
                new Option("severe", "Severe", 2))),
            new Question("q3", "Is it affecting your daily work or sleep?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1))),
            new Question("q4", "Any fever, bleeding, or breathing difficulty along with it?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 2))),
            new Question("q5", "Is this for a child, elderly person, or pregnant woman?", List.of(
                new Option("no", "No", 0),
                new Option("yes", "Yes", 1)))
        ));
    }

    // ---------- flow logic ----------

    public QuestionResponse start(String symptomText) {
        String category = matchCategory(symptomText);
        Question q1 = CATEGORY_QUESTIONS.get(category).get(0);
        return new QuestionResponse(category, 1, 5, q1);
    }

    public Object next(String category, int questionNumber, List<Answer> answers) {
        List<Question> questions = CATEGORY_QUESTIONS.getOrDefault(category, CATEGORY_QUESTIONS.get("GENERAL"));
        if (questionNumber < 5) {
            Question nextQ = questions.get(questionNumber); // 0-indexed; questionNumber = one just answered
            return new QuestionResponse(category, questionNumber + 1, 5, nextQ);
        }
        return buildResult(category, questions, answers);
    }

    private String matchCategory(String symptomText) {
        if (symptomText == null) return "GENERAL";
        String text = symptomText.toLowerCase(Locale.ROOT);
        for (Map.Entry<String, List<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String kw : entry.getValue()) {
                if (text.contains(kw)) return entry.getKey();
            }
        }
        return "GENERAL";
    }

    private TreatmentResult buildResult(String category, List<Question> questions, List<Answer> answers) {
        int severityScore = 0;
        boolean redFlag = false;

        for (Answer a : answers) {
            for (Question q : questions) {
                if (q.id().equals(a.questionId())) {
                    for (Option opt : q.options()) {
                        if (opt.value().equals(a.value())) {
                            severityScore += opt.severity();
                            if (opt.severity() >= 2) redFlag = true;
                        }
                    }
                }
            }
        }

        String severity;
        String advice;
        if (redFlag || severityScore >= 6) {
            severity = "urgent";
            advice = "Your answers suggest this needs prompt medical attention. Please avoid self-treating and "
                    + "visit a doctor or the nearest health centre as soon as possible. Meanwhile, rest, stay "
                    + "hydrated, and avoid exertion.";
        } else if (severityScore >= 3) {
            severity = "moderate";
            advice = "This looks like a moderate case of " + CATEGORY_LABEL.getOrDefault(category, "this problem")
                    + ". Rest well, drink plenty of fluids, eat light home-cooked food, and maintain hygiene. "
                    + "If it does not improve within 1-2 days, or gets worse, see a doctor rather than waiting.";
        } else {
            severity = "mild";
            advice = "This appears to be a mild case of " + CATEGORY_LABEL.getOrDefault(category, "this problem")
                    + ". Take rest, drink enough water/fluids, eat light meals, and maintain good hygiene. "
                    + "It should settle in a few days with basic home care.";
        }

        String specialty = CATEGORY_SPECIALTY.getOrDefault(category, GENERAL_PHYSICIAN);
        List<Doctor> doctors = doctorRepository.findBySpecialtyAndActiveTrue(specialty);
        if (doctors.isEmpty()) {
            doctors = doctorRepository.findByActiveTrue();
        }

        String consultNote = "This is general guidance only, not a medical diagnosis. "
                + "Please consult a doctor for proper examination and treatment.";

        return new TreatmentResult(category, severity, advice, consultNote, specialty, doctors);
    }
}
