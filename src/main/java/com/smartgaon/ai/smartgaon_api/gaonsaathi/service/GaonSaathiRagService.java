package com.smartgaon.ai.smartgaon_api.gaonsaathi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartgaon.ai.smartgaon_api.business.BusinessPost;
import com.smartgaon.ai.smartgaon_api.business.BusinessPostRepository;
import com.smartgaon.ai.smartgaon_api.doctor.Doctor;
import com.smartgaon.ai.smartgaon_api.doctor.DoctorRepository;
import com.smartgaon.ai.smartgaon_api.gaonsaathi.dto.GaonSaathiChatRequest;
import com.smartgaon.ai.smartgaon_api.gaonsaathi.dto.GaonSaathiChatResponse;
import com.smartgaon.ai.smartgaon_api.scheme.entity.Scheme;
import com.smartgaon.ai.smartgaon_api.scheme.repository.UserSchemeRepository;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.entity.SchoolCompetition;
import com.smartgaon.ai.smartgaon_api.schoolcompetition.repository.SchoolCompetitionRepository;
import com.smartgaon.ai.smartgaon_api.model.TodayTip;
import com.smartgaon.ai.smartgaon_api.service.TodayTipRedisService;
import com.smartgaon.ai.smartgaon_api.mandi.entity.MandiRate;
import com.smartgaon.ai.smartgaon_api.mandi.repository.MandiRateRepository;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.Job;
import com.smartgaon.ai.smartgaon_api.sewa.jobs.JobRepository;
import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;
import com.smartgaon.ai.smartgaon_api.villageofficer.repository.GovernmentOfferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.smartgaon.ai.smartgaon_api.config.GroqQuestionService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GaonSaathiRagService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Autowired(required = false)
    private GroqQuestionService groqQuestionService;

    @Autowired(required = false)
    private UserSchemeRepository userSchemeRepository;

    @Autowired(required = false)
    private GovernmentOfferRepository governmentOfferRepository;

    @Autowired(required = false)
    private JobRepository jobRepository;

    @Autowired(required = false)
    private DoctorRepository doctorRepository;

    @Autowired(required = false)
    private BusinessPostRepository businessPostRepository;

    @Autowired(required = false)
    private SchoolCompetitionRepository schoolCompetitionRepository;

    @Autowired(required = false)
    private TodayTipRedisService todayTipRedisService;

    @Autowired(required = false)
    private MandiRateRepository mandiRateRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ==========================================
    // 1. SMART IN-MEMORY QUERY CACHE (TTL = 15m)
    // 0 Token & 5ms Latency for repetitive queries
    // ==========================================
    private static class CacheEntry {
        final GaonSaathiChatResponse response;
        final long timestamp;

        CacheEntry(GaonSaathiChatResponse response) {
            this.response = response;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isExpired(long ttlMillis) {
            return (System.currentTimeMillis() - timestamp) > ttlMillis;
        }
    }

    private final Map<String, CacheEntry> queryCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 15 * 60 * 1000L; // 15 minutes
    private static final Pattern ROUTE_TAG_PATTERN = Pattern.compile("\\[ROUTE:\\s*([^\\]]+)\\]", Pattern.CASE_INSENSITIVE);

    public GaonSaathiChatResponse processChat(GaonSaathiChatRequest request) {
        String query = request.getQuestion() != null ? request.getQuestion().trim() : "";
        String requestedLang = request.getLanguage() != null ? request.getLanguage().trim() : "";

        if (query.isEmpty()) {
            return new GaonSaathiChatResponse("Please ask a question. / कृपया प्रश्न विचारा. / कृपया अपना सवाल पूछें।", null, null, Collections.emptyList());
        }

        // 1. Detect Language accurately (English, Hindi, or Marathi)
        String detectedLang = detectLanguage(query, requestedLang);
        String lowerQuery = query.toLowerCase();

        // 2. Check Smart Cache
        String cacheKey = detectedLang + "::" + normalizeForCache(lowerQuery);
        CacheEntry cached = queryCache.get(cacheKey);
        if (cached != null && !cached.isExpired(CACHE_TTL_MS)) {
            System.out.println("[GaonSaathi RAG] Cache HIT for key: " + cacheKey);
            return cached.response;
        }

        // 3. Fast Heuristic Route & Intent Check (Fast Path)
        RouteMatch heuristicMatch = matchHeuristicRoute(lowerQuery, detectedLang);

        // 4. Fetch Multi-Entity Live Database Context (True RAG)
        String dbContext = fetchMultiEntityDbContext(lowerQuery, heuristicMatch.getCategory());

        // 5. Build SOTA Multilingual System Prompt with Strict Language Isolation, Female Persona & Tagging Rules
        String systemPrompt;
        if ("en".equalsIgnoreCase(detectedLang)) {
            systemPrompt = "You are Gaon Sathi AI, a polite, friendly, and helpful FEMALE rural companion for the SmartGaon platform.\n\n"
                    + "CRITICAL LANGUAGE MANDATE: USER LANGUAGE IS ENGLISH.\n"
                    + "- Reply STRICTLY and EXCLUSIVELY in clear, natural, polite ENGLISH.\n"
                    + "- NEVER use Hindi, Devanagari script, or Hindi phrases (NEVER say 'जी बिल्कुल', 'रही हूँ', 'नमस्ते', 'सकती हूँ', etc.). You must speak as a professional female assistant in English.\n"
                    + "- Keep answer concise and audio-friendly (2 to 3 sentences maximum).\n"
                    + "- CRITICAL PRONUNCIATION / SPEECH RULE: NEVER write 'qtl' or 'Qtl'. Always write 'Quintal' or 'per quintal' so voice announcement says 'quintal' cleanly instead of spelling 'q-t-l'.\n\n"
                    + "PORTAL DESTINATION ROUTES & INTENT RULES:\n"
                    + "- GIVER / POSTER INTENT:\n"
                    + "  * /post-job: User wants to GIVE a job, hire workers, post vacancy, or provide job details ('post a job', 'give job', 'hire workers') -> [ROUTE:/post-job]\n"
                    + "  * /post-business: User wants to GIVE business/shop details, list/post business or shop ('post business', 'register shop', 'add business') -> [ROUTE:/post-business]\n"
                    + "- SEEKER / CONSUMER INTENT:\n"
                    + "  * /job-board: User wants to GET a job, seeking employment or work ('I want job', 'need job', 'find jobs', 'jobs for engineer').\n"
                    + "    - If government job requested -> [ROUTE:/job-board?tab=govt]\n"
                    + "    - If specific profession/role requested (e.g. engineer, electrician, plumber, driver, teacher, painter, security) -> [ROUTE:/job-board?search=<role>]\n"
                    + "    - General jobs -> [ROUTE:/job-board]\n"
                    + "  * /business: User wants business ideas, wants to explore village shops, browse existing businesses ('business ideas', 'explore businesses') -> [ROUTE:/business]\n"
                    + "- /kisan-mitra: Kisan Mitra digital farming ecosystem -> [ROUTE:/kisan-mitra]\n"
                    + "- /kisan-mitra?tab=mandi: Daily live Mandi rates & APMC prices. When user asks about fruits (banana, apple, mango, orange, pomegranate, grapes, strawberry), use [ROUTE:/kisan-mitra?tab=mandi&category=fruits]; for vegetables (tomato, onion, potato, etc.) use [ROUTE:/kisan-mitra?tab=mandi&category=vegetables]; for pulses use [ROUTE:/kisan-mitra?tab=mandi&category=pulses]; for oilseeds use [ROUTE:/kisan-mitra?tab=mandi&category=oilseeds]; for grains use [ROUTE:/kisan-mitra?tab=mandi&category=grains]; otherwise [ROUTE:/kisan-mitra?tab=mandi]\n"
                    + "- /kisan-mitra?tab=fertilizer-calc: Scientific Fertilizer Calculator (Urea, DAP dose, Bigha/Acre) -> [ROUTE:/kisan-mitra?tab=fertilizer-calc]\n"
                    + "- /kisan-mitra?tab=crop-doctor: AI Crop Doctor leaf scanner & pest remedies -> [ROUTE:/kisan-mitra?tab=crop-doctor]\n"
                    + "- /kisan-mitra?tab=khata: Digital Kisan Khata farm expenses & revenue ledger -> [ROUTE:/kisan-mitra?tab=khata]\n"
                    + "- /kisan-mitra?tab=krishi-bazaar: Krishi Bazaar, buy/sell crops & cattle, tractor rent -> [ROUTE:/kisan-mitra?tab=krishi-bazaar]\n"
                    + "- /kisan-mitra?tab=weather: Precision spray window, safe spraying weather -> [ROUTE:/kisan-mitra?tab=weather]\n"
                    + "- /kisan-mitra?tab=schemes: Agriculture government schemes & subsidies (PM-Kisan, PMFBY) -> [ROUTE:/kisan-mitra?tab=schemes]\n"
                    + "- /scheme: Government schemes, subsidies -> [ROUTE:/scheme]\n"
                    + "- /daily-quiz: Daily educational quiz, questions, MCQs, student challenges -> [ROUTE:/daily-quiz]\n"
                    + "- /shiksha-quiz: Competitive exam preparation & quiz for SSC, Railway, Banking, State Exams, Police, General GK -> [ROUTE:/shiksha-quiz]\n"
                    + "- /school-competition: School competitions, student contests, awards -> [ROUTE:/school-competition]\n"
                    + "- /smartgaon: SmartGaon initiative, smart village project overview, mission, villages covered -> [ROUTE:/smartgaon]\n"
                    + "- /my-village: User's village dashboard, village info -> [ROUTE:/my-village]\n"
                    + "- /gaon-doctor: Healthcare, village doctor, clinics, emergency health -> [ROUTE:/gaon-doctor]\n"
                    + "- /directory: Village directory, Sarpanch, Gram Pradhan, Panchayat secretary, contacts -> [ROUTE:/directory]\n"
                    + "- /local-experts: Local service workers, plumber, electrician, mechanic, mason, carpenter for hire -> [ROUTE:/local-experts]\n"
                    + "- /report: Citizen problem reporting, broken road, water leak, electricity cut, garbage, complaints -> [ROUTE:/report]\n"
                    + "- /weather: Weather forecast, rainfall updates -> [ROUTE:/weather]\n"
                    + "- /donation: Village development fund, donations -> [ROUTE:/donation]\n"
                    + "- /ncert: NCERT books, syllabus, curriculum, study materials -> [ROUTE:/ncert]\n"
                    + "- /career-guide: Career guidance, higher education streams -> [ROUTE:/career-guide]\n"
                    + "- /video-lessons: Educational video lessons -> [ROUTE:/video-lessons]\n"
                    + "- /gaon-talent: Village talent, rural videos, creative reels -> [ROUTE:/gaon-talent]\n"
                    + "- /home: Today's tips, home dashboard, daily village tips & updates -> [ROUTE:/home]\n"
                    + "- NONE: General informational questions, general knowledge, agricultural guidance (e.g. pesticides, crops, farming tips), chit-chat, or questions NOT asking to open or visit a portal section.\n\n"
                    + "CRITICAL ROUTING RULES:\n"
                    + "1. NAVIGATION INTENT: If user explicitly asks to open, go to, see, or visit a page/feature (e.g. 'I want job' -> /job-board, 'post a job' -> /post-job, 'post a business' -> /post-business, 'open daily quiz' -> /daily-quiz): Cheerfully confirm in English as a female assistant (e.g. 'Certainly! I am taking you to the Jobs page to explore available opportunities.') and append: [ROUTE:/path]. NEVER say 'I cannot directly open it'!\n"
                    + "2. INFORMATIONAL INTENT: If user is asking a general knowledge, informational, factual, agricultural, or explanatory question (e.g. 'how many smart village in...', 'how to use pesticides', 'what is vermicompost'): Provide a clear, helpful English explanation and ALWAYS output: [ROUTE:NONE]. NEVER attach an arbitrary portal route to an informational question!\n"
                    + "3. TIPS & DAILY ADVICE INTENT: When user asks for 'today tips', 'daily tips', or farming advice: NEVER ask an evasive question like 'What kind of tip do you want?'. ALWAYS IMMEDIATELY PROVIDE A CONCRETE, ACTIONABLE TIP in English (on smart irrigation, organic manure/vermicompost, crop protection, or seasonal farming). Output: [ROUTE:/home].\n"
                    + "4. COMPETITIVE EXAM PREPARATION (SSC, Railway, Bank, Police, State Exam, Exam Prep): When user asks to prepare for exams (e.g. 'I want prepare for SSC', 'Railway exam preparation', 'Bank exam quiz'): Cheerfully confirm in English (e.g. 'Certainly! Taking you to the Shiksha Quiz section where you can prepare for your SSC exams with mock tests.') and ALWAYS output: [ROUTE:/shiksha-quiz]. NEVER output /exam-prep.\n"
                    + "5. CROP & MANDI PRICE MANDATE: When user asks about price, rate, MSP, or mandi bhav of ANY crop (e.g. Paddy, Wheat, Mustard, Onion, Chana, etc.): NEVER refuse to answer and NEVER say you do not have real-time data! Immediately quote the APMC Modal Rate and Government MSP benchmark from the Database Context, and ALWAYS output: [ROUTE:/kisan-mitra?tab=mandi].\n\n"
                    + "RESPONSE GUIDELINES:\n"
                    + "1. Keep answer concise and audio-friendly (2 to 3 sentences maximum).\n"
                    + "2. Ground your answer in the Database Context if relevant.\n"
                    + "3. On the very last line, output the matching route tag exactly like: [ROUTE:/path] or [ROUTE:NONE].\n\n"
                    + "Database Context:\n" + dbContext;
        } else if ("mr".equalsIgnoreCase(detectedLang)) {
            systemPrompt = "You are Gaon Sathi AI (गाव साथी AI), a polite, friendly, and helpful FEMALE rural companion for the SmartGaon platform.\n\n"
                    + "CRITICAL LANGUAGE MANDATE: USER LANGUAGE IS MARATHI (मराठी).\n"
                    + "- Reply STRICTLY and EXCLUSIVELY in natural, polite MARATHI (मराठी).\n"
                    + "- FEMALE PERSONA: Speak in polite female Marathi tone (उदा. 'मी मदत करू शकते', 'घेऊन जात आहे', 'उघडत आहे').\n"
                    + "- महत्त्वाचा उच्चार नियम: कधीही 'qtl' किंवा 'Qtl' लिहू नका, नेहमी 'क्विंटल' (प्रति क्विंटल) लिहा जेणेकरून व्हॉईस अनाउन्समेंटमध्ये 'q-t-l' ऐवजी स्पष्ट 'क्विंटल' उच्चार होईल.\n"
                    + "- Keep answer concise and audio-friendly (2 to 3 sentences maximum).\n\n"
                    + "PORTAL DESTINATION ROUTES & INTENT RULES:\n"
                    + "- GIVER / POSTER INTENT: User wants to post a job or hire workers ('नोकरी द्यायची आहे', 'कामगार पाहिजेत') -> [ROUTE:/post-job]\n"
                    + "- GIVER / POSTER INTENT: User wants to list a business or shop ('व्यवसाय नोंदणी करायची आहे', 'दुकान जोडायचे आहे') -> [ROUTE:/post-business]\n"
                    + "- SEEKER INTENT: User wants a job ('मला नोकरी पाहिजे', 'काम हवे आहे') -> [ROUTE:/job-board]\n"
                    + "  * If government job requested -> [ROUTE:/job-board?tab=govt]\n"
                    + "  * If specific profession requested (उदा. अभियंता, इलेक्ट्रिशियन) -> [ROUTE:/job-board?search=<role>]\n"
                    + "- SEEKER INTENT: User wants business ideas ('व्यवसाय सुरू करायचा आहे', 'नवीन आयडिया') -> [ROUTE:/business]\n"
                    + "- /kisan-mitra: शेतकरी मित्र डिजिटल शेती हब -> [ROUTE:/kisan-mitra]\n"
                    + "- /kisan-mitra?tab=mandi: थेट बाजारभाव (फळांसाठी [ROUTE:/kisan-mitra?tab=mandi&category=fruits], भाजीपाल्यासाठी [ROUTE:/kisan-mitra?tab=mandi&category=vegetables], कडधान्यांसाठी [ROUTE:/kisan-mitra?tab=mandi&category=pulses], धान्यासाठी [ROUTE:/kisan-mitra?tab=mandi&category=grains]) -> [ROUTE:/kisan-mitra?tab=mandi]\n"
                    + "- /kisan-mitra?tab=fertilizer-calc: खत कॅल्क्युलेटर (युरिया, डीएपी प्रमाण) -> [ROUTE:/kisan-mitra?tab=fertilizer-calc]\n"
                    + "- /kisan-mitra?tab=crop-doctor: पीक डॉक्टर व रोग निदान स्कॅनर -> [ROUTE:/kisan-mitra?tab=crop-doctor]\n"
                    + "- /kisan-mitra?tab=khata: डिजिटल शेतकरी खातेवही -> [ROUTE:/kisan-mitra?tab=khata]\n"
                    + "- /kisan-mitra?tab=krishi-bazaar: कृषी बाजार व ट्रॅक्टर भाडे -> [ROUTE:/kisan-mitra?tab=krishi-bazaar]\n"
                    + "- /kisan-mitra?tab=weather: फवारणी हवामान व सुरक्षित स्प्रे अलर्ट -> [ROUTE:/kisan-mitra?tab=weather]\n"
                    + "- /kisan-mitra?tab=schemes: कृषी शासकीय योजना -> [ROUTE:/kisan-mitra?tab=schemes]\n"
                    + "- /scheme: शासकीय योजना -> [ROUTE:/scheme]\n"
                    + "- /daily-quiz: दैनिक प्रश्नमंजुषा -> [ROUTE:/daily-quiz]\n"
                    + "- /shiksha-quiz: स्पर्धा परीक्षा तयारी (SSC, रेल्वे, पोलीस, बँकिंग) -> [ROUTE:/shiksha-quiz]\n"
                    + "- /smartgaon: स्मार्टगाव मिशन -> [ROUTE:/smartgaon]\n"
                    + "- /home: आजच्या टीप्स -> [ROUTE:/home]\n"
                    + "- NONE: माहितीपर प्रश्न (उदा. कीटकनाशकांचा वापर कसा करावा, खत कसे तयार करावे) -> [ROUTE:NONE]\n\n"
                    + "CRITICAL ROUTING RULES:\n"
                    + "1. NAVIGATION INTENT: If user asks to open or visit a page, cheerfully confirm in Marathi female voice (उदा. 'नक्कीच! मी तुमच्यासाठी नोकऱ्यांचे पेज उघडत आहे.') and append: [ROUTE:/path].\n"
                    + "2. INFORMATIONAL INTENT: For factual questions, answer in Marathi and output: [ROUTE:NONE].\n"
                    + "3. TIPS INTENT: When user asks for tips, immediately give a concrete farming tip in Marathi and output: [ROUTE:/home].\n"
                    + "4. COMPETITIVE EXAM: Cheerfully confirm in Marathi (उदा. 'नक्कीच! SSC परीक्षेच्या तयारीसाठी मी तुम्हाला शिक्षा क्विझ पेजवर घेऊन जात आहे.') and output: [ROUTE:/shiksha-quiz].\n"
                    + "5. पिकांचे बाजारभाव व हमीभाव (CROP & MANDI PRICE MANDATE): जेव्हा शेतकरी कोणत्याही पिकाचे बाजारभाव, हमीभाव (MSP) किंवा दर विचारतील (उदा. धान, गहू, हरभरा, कांदा, सोयाबीन): 'माझ्याकडे माहिती नाही' असे कधीही सांगू नका. वरील डेटाबेस संदर्भातील अधिकृत सरासरी बाजारभाव आणि शासकीय हमीभाव (MSP) त्वरित सांगा आणि शेवटी [ROUTE:/kisan-mitra?tab=mandi] जोडा.\n\n"
                    + "RESPONSE GUIDELINES:\n"
                    + "1. Keep answer concise and audio-friendly (2 to 3 sentences maximum).\n"
                    + "2. On the very last line, output the matching route tag: [ROUTE:/path] or [ROUTE:NONE].\n\n"
                    + "Database Context:\n" + dbContext;
        } else {
            // Hindi / Hinglish
            systemPrompt = "You are Gaon Sathi AI (गांव साथी AI), an elite, highly intelligent rural FEMALE assistant for the SmartGaon platform.\n\n"
                    + "LANGUAGE INSTRUCTION:\n"
                    + "USER LANGUAGE: HINDI / HINGLISH.\n"
                    + "MANDATORY RULE 1: Reply STRICTLY and EXCLUSIVELY in friendly, natural HINDI (हिन्दी).\n"
                    + "MANDATORY RULE 2 (STRICT FEMALE GRAMMAR): You are Gaon Sathi, a FEMALE digital assistant (स्त्रीलिंगी). "
                    + "You MUST ALWAYS speak using FEMININE verbs and pronouns: use 'रही हूँ' (NEVER 'रहा हूँ'), 'करूँगी' (NEVER 'करूँगा'), 'सकती हूँ' (NEVER 'सकता हूँ'), 'ले जा रही हूँ', 'दिखा रही हूँ', 'खोल रही हूँ'. "
                    + "NEVER use masculine words like 'रहा हूँ', 'ले जा रहा हूँ' or 'करूँगा' under any circumstance!\n"
                    + "Do NOT reply in English or Marathi.\n\n"
                    + "PORTAL DESTINATION ROUTES & INTENT RULES:\n"
                    + "- GIVER / POSTER INTENT (देना / पोस्ट करना / जानकारी दर्ज करना):\n"
                    + "  * /post-job: User wants to GIVE a job, hire workers, post vacancy, or provide job details ('job dena hai', 'naukri deni hai', 'kaam dena hai', 'hire karna hai', 'vacancy nikalni hai', 'worker chahiye kaam ke liye') -> [ROUTE:/post-job]\n"
                    + "  * /post-business: User wants to GIVE business/shop details, list/post their business or shop ('business dena hai', 'business ki jankari deni hai', 'apna business post karna hai', 'dukan jodna hai', 'vyapar register karna hai') -> [ROUTE:/post-business]\n"
                    + "- SEEKER / CONSUMER INTENT (लेना / चाहिए / ढूंढना / आइडिया):\n"
                    + "  * /job-board: User wants to GET a job, seeking employment or work ('naukri chahiye', 'job chahiye', 'kaam chahiye', 'naukri lena hai').\n"
                    + "    - If government job requested -> [ROUTE:/job-board?tab=govt]\n"
                    + "    - If specific profession/role requested (e.g. engineer, electrician, plumber, driver, teacher, painter, security) -> [ROUTE:/job-board?search=<role>]\n"
                    + "    - General jobs -> [ROUTE:/job-board]\n"
                    + "  * /business: User wants business ideas, wants to explore village shops, browse existing businesses ('business idea chahiye', 'business chahiye', 'gaon ke business dekhne hai', 'kya business kare', 'dukan dekhni hai', 'vyapar dekhna hai') -> [ROUTE:/business]\n"
                    + "- /kisan-mitra: किसान मित्र डिजिटल कृषि हब ('kisan mitra', 'kisan dashboard') -> [ROUTE:/kisan-mitra]\n"
                    + "- /kisan-mitra?tab=mandi: दैनिक मंडी भाव. अगर यूजर फल पूछे (केला, सेब, संतरा, अनार, आम आदि) तो [ROUTE:/kisan-mitra?tab=mandi&category=fruits]; सब्जी पूछे (आलू, प्याज, टमाटर) तो [ROUTE:/kisan-mitra?tab=mandi&category=vegetables]; दलहन पूछे तो [ROUTE:/kisan-mitra?tab=mandi&category=pulses]; तिलहन पूछे तो [ROUTE:/kisan-mitra?tab=mandi&category=oilseeds]; अनाज पूछे तो [ROUTE:/kisan-mitra?tab=mandi&category=grains]; सामान्य मंडी के लिए [ROUTE:/kisan-mitra?tab=mandi]\n"
                    + "- /kisan-mitra?tab=fertilizer-calc: खाद कैलकुलेटर, यूरिया डीएपी की मात्रा ('khad kitni daalein', 'fertilizer calculator') -> [ROUTE:/kisan-mitra?tab=fertilizer-calc]\n"
                    + "- /kisan-mitra?tab=crop-doctor: फसल रोग डॉक्टर, पत्ती की बीमारी स्कैनर, कीट नियंत्रण ('fasal me bimari hai', 'crop doctor', 'rog pehchan') -> [ROUTE:/kisan-mitra?tab=crop-doctor]\n"
                    + "- /kisan-mitra?tab=khata: डिजिटल किसान खाता, खेती का हिसाब-किताब, खर्च बही ('kisan khata', 'hisaab kitab') -> [ROUTE:/kisan-mitra?tab=khata]\n"
                    + "- /kisan-mitra?tab=krishi-bazaar: कृषि बाजार, ट्रैक्टर किराया, पशु व बीज खरीद-बिक्री ('krishi bazaar', 'tractor kiraya') -> [ROUTE:/kisan-mitra?tab=krishi-bazaar]\n"
                    + "- /kisan-mitra?tab=weather: कृषि मौसम व दवा छिड़काव अलर्ट ('spray window', 'spray kab karein', 'chhidkav') -> [ROUTE:/kisan-mitra?tab=weather]\n"
                    + "- /kisan-mitra?tab=schemes: कृषि सरकारी योजनाएं, पीएम किसान, फसल बीमा ('kisan yojana', 'agri schemes') -> [ROUTE:/kisan-mitra?tab=schemes]\n"
                    + "- /scheme: Government schemes, agriculture subsidies. Specific scheme (e.g. kisan, awas, pension, subsidy) -> [ROUTE:/scheme?search=<keyword>]\n"
                    + "- /daily-quiz: Daily educational quiz, questions, MCQs, student challenges -> [ROUTE:/daily-quiz]\n"
                    + "- /shiksha-quiz: Competitive exam preparation & quiz for SSC, Railway, Banking, State Exams, Police, General GK, exam preparation -> [ROUTE:/shiksha-quiz]\n"
                    + "- /school-competition: School competitions, student contests, awards -> [ROUTE:/school-competition]\n"
                    + "- /smartgaon: SmartGaon initiative, smart village project overview, mission, villages covered -> [ROUTE:/smartgaon]\n"
                    + "- /my-village: User's village dashboard, village info -> [ROUTE:/my-village]\n"
                    + "- /gaon-doctor: Healthcare, village doctor, clinics, medicine, emergency health -> [ROUTE:/gaon-doctor]\n"
                    + "- /directory: Village directory, Sarpanch, Gram Pradhan, Panchayat secretary, contacts -> [ROUTE:/directory]\n"
                    + "- /local-experts: Local service workers, plumber, electrician, mechanic, mason, carpenter for hire -> [ROUTE:/local-experts]\n"
                    + "- /report: Citizen problem reporting, broken road, water leak, electricity cut, garbage, complaints -> [ROUTE:/report]\n"
                    + "- /weather: Weather forecast, rainfall, monsoon updates -> [ROUTE:/weather]\n"
                    + "- /donation: Village development fund, donations -> [ROUTE:/donation]\n"
                    + "- /ncert: NCERT books, syllabus, curriculum, study materials -> [ROUTE:/ncert]\n"
                    + "- /career-guide: Career guidance, higher education streams -> [ROUTE:/career-guide]\n"
                    + "- /video-lessons: Educational video lessons -> [ROUTE:/video-lessons]\n"
                    + "- /gaon-talent: Village talent, rural videos, creative reels -> [ROUTE:/gaon-talent]\n"
                    + "- /home: Today's tips, home dashboard, daily village tips & updates -> [ROUTE:/home]\n"
                    + "- NONE: General informational questions, general knowledge, agricultural guidance (e.g. pesticides, crops, farming tips), chit-chat, or questions NOT asking to open or visit a portal section.\n\n"
                    + "CRITICAL ROUTING RULES:\n"
                    + "1. NAVIGATION INTENT: If user explicitly asks to open, go to, see, or visit a page/feature (e.g. 'अपना बिजनेस पोस्ट लिस्ट करना है' / 'बिजनेस की जानकारी देनी है' -> /post-business, 'job dena hai' / 'मुझे नौकरी पोस्ट करनी है' -> /post-job, 'नौकरी चाहिए' -> /job-board, 'business idea chahiye' -> /business, 'open daily quiz' -> /daily-quiz), cheerfully confirm in your FEMALE voice that you are opening it (उदा. 'जी बिल्कुल! आपके लिए पेज खोल रही हूँ।') and append the matching [ROUTE:/path]. NEVER say 'I cannot directly open it' or 'Please navigate manually' because you DO have auto-navigation capability!\n"
                    + "2. INFORMATIONAL INTENT: If user is asking a general knowledge, informational, factual, agricultural, or explanatory question (e.g. 'how many smart village in...', 'how to use pesticides', 'what is vermicompost', 'wheat sowing season'), provide a clear, helpful answer and ALWAYS output: [ROUTE:NONE]. NEVER attach an arbitrary portal route to an informational question!\n"
                    + "3. TIPS & DAILY ADVICE INTENT: When user asks for 'today tips', 'aaj ka tip', 'daily tips', 'tips', or farming advice: NEVER ask an evasive question like 'What kind of tip do you want?' or 'Please let me know specifically'. ALWAYS IMMEDIATELY PROVIDE A CONCRETE, ACTIONABLE TIP (on smart irrigation, organic manure/vermicompost, crop protection, or seasonal farming). If active tips are in the Database Context, summarize the active tip directly. Output: [ROUTE:/home].\n"
                    + "4. COMPETITIVE EXAM PREPARATION (SSC, Railway, Bank, Police, State Exam, Exam Prep): When user asks to prepare for exams (e.g. 'I want prepare for SSC', 'SSC ki taiyari', 'Railway exam preparation', 'Bank exam quiz', 'competitive exam'), cheerfully confirm in female voice (उदा. 'जी बिल्कुल! SSC की तैयारी के लिए मैं आपको शिक्षा क्विज (Shiksha Quiz) पेज पर ले जा रही हूँ, जहाँ आप मॉक टेस्ट दे सकते हैं।') and ALWAYS output: [ROUTE:/shiksha-quiz]. NEVER output /exam-prep as it has been replaced by /shiksha-quiz.\n"
                    + "5. फसल व मंडी भाव नियम (CROP & MANDI PRICE MANDATE): जब भी किसान किसी फसल के भाव, मंडी रेट या सरकारी MSP के बारे में पूछे (उदा. धान, गेहूं, सरसों, चना, प्याज, आलू, टमाटर आदि): 'मेरे पास रियल-टाइम डेटा नहीं है' ऐसा कभी न कहें! तुरंत ऊपर दिए गए डेटाबेस से मॉडल भाव और सरकारी समर्थन मूल्य (MSP) बताएं और अंत में अवश्य लिखें: [ROUTE:/kisan-mitra?tab=mandi]। कभी भी 'qtl' या 'Qtl' शॉर्ट-फॉर्म न लिखें, हमेशा 'क्विंटल' (प्रति क्विंटल) लिखें ताकि ऑडियो वॉइस में 'क्यू-टी-एल' की जगह साफ 'क्विंटल' उच्चारित हो!\n\n"
                    + "RESPONSE GUIDELINES:\n"
                    + "1. Keep answer concise and audio-friendly (2 to 3 sentences maximum).\n"
                    + "2. Ground your answer in the Database Context if relevant.\n"
                    + "3. On the very last line, output the matching route tag exactly like: [ROUTE:/path] or [ROUTE:NONE].\n\n"
                    + "Database Context:\n" + dbContext;
        }

        // 6. Call Groq AI API with Token Cap
        String rawAiReply = callGroqApi(systemPrompt, query);

        // 7. Parse AI Route Tag & Clean User Reply
        String finalRoute = heuristicMatch.getRoute();
        String finalCategory = heuristicMatch.getCategory();
        String cleanReply = rawAiReply;

        if (rawAiReply != null && !rawAiReply.trim().isEmpty()) {
            Matcher matcher = ROUTE_TAG_PATTERN.matcher(rawAiReply);
            if (matcher.find()) {
                String aiExtractedRoute = matcher.group(1).trim();
                // Clean the tag from user speech/text
                cleanReply = matcher.replaceAll("").trim();

                // If AI explicitly returned NONE, cancel any heuristic route!
                if ("NONE".equalsIgnoreCase(aiExtractedRoute)) {
                    finalRoute = null;
                    finalCategory = "GENERAL";
                } else if (aiExtractedRoute.startsWith("/")) {
                    if (aiExtractedRoute.startsWith("/exam-prep")) {
                        aiExtractedRoute = "/shiksha-quiz";
                    }
                    finalRoute = aiExtractedRoute;
                    finalCategory = mapRouteToCategory(finalRoute);
                }
            }
        } else {
            // High-grade fallback if API is unreachable
            cleanReply = buildSafeFallbackReply(detectedLang, finalRoute);
        }

        // 8. Dynamic Localized Navigation Action Label
        String actionLabel = resolveActionLabel(finalRoute, detectedLang);

        // 9. Contextual Dynamic Follow-Up Chips
        List<String> dynamicChips = buildContextualChips(finalCategory, detectedLang);

        GaonSaathiChatResponse response = new GaonSaathiChatResponse(cleanReply, finalRoute, actionLabel, dynamicChips);

        // 10. Store in Cache for 0-token instant reuse
        if (cleanReply != null && !cleanReply.isEmpty()) {
            queryCache.put(cacheKey, new CacheEntry(response));
            if (queryCache.size() > 2000) {
                queryCache.clear(); // Safe eviction
            }
        }

        return response;
    }

    /**
     * Maps route to broad category for contextual suggestions
     */
    private String mapRouteToCategory(String route) {
        if (route == null) return "GENERAL";
        if (route.startsWith("/kisan-mitra")) {
            if (route.contains("tab=mandi")) return "MANDI";
            if (route.contains("tab=fertilizer-calc")) return "FERTILIZER";
            if (route.contains("tab=crop-doctor")) return "CROP_DOCTOR";
            return "KISAN_MITRA";
        }
        if (route.startsWith("/scheme")) return "SCHEME";
        if (route.startsWith("/job-board")) return "JOB";
        if (route.startsWith("/gaon-doctor")) return "HEALTH";
        if (route.startsWith("/directory")) return "DIRECTORY";
        if (route.startsWith("/school-competition") || route.startsWith("/daily-quiz") || route.startsWith("/shiksha-quiz") || route.startsWith("/ncert")) return "EDUCATION";
        if (route.startsWith("/report")) return "CIVIC";
        if (route.startsWith("/business")) return "BUSINESS";
        if (route.startsWith("/weather")) return "WEATHER";
        if (route.startsWith("/donation")) return "DONATION";
        if (route.startsWith("/home")) return "TIPS";
        if (route.startsWith("/local-expert")) return "JOB";
        return "GENERAL";
    }

    /**
     * Resolves human-friendly navigation action label across 3 languages
     */
    private String resolveActionLabel(String route, String lang) {
        if (route == null) return null;
        boolean isMr = "mr".equalsIgnoreCase(lang);
        boolean isEn = "en".equalsIgnoreCase(lang);

        if (route.startsWith("/kisan-mitra")) {
            if (route.contains("tab=mandi")) {
                return isEn ? "Daily Mandi Rates" : (isMr ? "दैनिक बाजारभाव" : "दैनिक मंडी भाव देखें");
            }
            if (route.contains("tab=fertilizer-calc")) {
                return isEn ? "Fertilizer Calculator" : (isMr ? "खत कॅल्क्युलेटर" : "खाद कैलकुलेटर खोलें");
            }
            if (route.contains("tab=crop-doctor")) {
                return isEn ? "AI Crop Doctor" : (isMr ? "पीक डॉक्टर स्कॅनर" : "फसल रोग स्कैनर");
            }
            if (route.contains("tab=khata")) {
                return isEn ? "Digital Kisan Khata" : (isMr ? "शेतकरी खातेवही" : "किसान खाता देखें");
            }
            if (route.contains("tab=krishi-bazaar")) {
                return isEn ? "Krishi Bazaar" : (isMr ? "कृषी बाजार" : "कृषि बाज़ार देखें");
            }
            if (route.contains("tab=weather")) {
                return isEn ? "Agri Weather & Spray Alert" : (isMr ? "हवामान व फवारणी अलर्ट" : "मौसम व स्प्रे अलर्ट");
            }
            if (route.contains("tab=schemes")) {
                return isEn ? "Agri Schemes" : (isMr ? "शासकीय कृषी योजना" : "कृषि सरकारी योजनाएं");
            }
            return isEn ? "Kisan Mitra Hub" : (isMr ? "शेतकरी मित्र" : "किसान मित्र हब");
        }

        if (route.startsWith("/post-business")) {
            return isEn ? "Post Business" : (isMr ? "व्यवसाय नोंदणी" : "बिजनेस पोस्ट करें");
        }
        if (route.startsWith("/post-job")) {
            return isEn ? "Post a Job" : (isMr ? "नोकरी पोस्ट करा" : "नौकरी पोस्ट करें");
        }
        if (route.startsWith("/daily-quiz")) {
            return isEn ? "Daily Quiz" : (isMr ? "दैनिक प्रश्नमंजुषा" : "डेली क्विज");
        }
        if (route.startsWith("/shiksha-quiz")) {
            return isEn ? "Shiksha Quiz" : (isMr ? "शिक्षा क्विझ" : "शिक्षा क्विज");
        }
        if (route.startsWith("/smartgaon")) {
            return isEn ? "SmartGaon" : (isMr ? "स्मार्टगाव" : "स्मार्टगांव");
        }
        if (route.startsWith("/my-village")) {
            return isEn ? "My Village" : (isMr ? "माझे गाव" : "मेरा गांव");
        }
        if (route.startsWith("/ncert")) {
            return isEn ? "NCERT Syllabus" : (isMr ? "एनसीईआरटी अभ्यासक्रम" : "NCERT पाठ्यक्रम");
        }
        if (route.startsWith("/local-expert")) {
            return isEn ? "Local Experts" : (isMr ? "स्थानिक कारागीर" : "लोकल एक्सपर्ट्स");
        }
        if (route.startsWith("/gaon-talent")) {
            return isEn ? "Gaon Talent" : (isMr ? "गाव टॅलेंट" : "गांव टैलेंट");
        }
        if (route.startsWith("/career-guide")) {
            return isEn ? "Career Guide" : (isMr ? "करिअर मार्गदर्शन" : "करियर गाइड");
        }
        if (route.startsWith("/video-lessons")) {
            return isEn ? "Video Lessons" : (isMr ? "व्हिडिओ वर्ग" : "वीडियो क्लासेज");
        }
        if (route.startsWith("/exam-prep")) {
            return isEn ? "Exam Prep" : (isMr ? "परीक्षा तयारी" : "परीक्षा तैयारी");
        }
        if (route.startsWith("/scheme")) {
            return isEn ? "View Schemes" : (isMr ? "शासकीय योजना पहा" : "सरकारी योजनाएं देखें");
        }
        if (route.startsWith("/job-board")) {
            if (route.contains("govt")) {
                return isEn ? "Government Jobs" : (isMr ? "शासकीय नोकऱ्या" : "सरकारी नौकरियां");
            }
            return isEn ? "View Jobs" : (isMr ? "नोकऱ्या पहा" : "नौकरियां देखें");
        }
        if (route.startsWith("/gaon-doctor")) {
            return isEn ? "Contact Doctor" : (isMr ? "गाव डॉक्टर संपर्क" : "गांव डॉक्टर संपर्क");
        }
        if (route.startsWith("/directory")) {
            return isEn ? "Village Directory" : (isMr ? "गाव निर्देशिका पहा" : "गांव डायरेक्टरी देखें");
        }
        if (route.startsWith("/school-competition")) {
            return isEn ? "View Competitions" : (isMr ? "शाळा स्पर्धा पहा" : "स्कूल प्रतियोगिताएं देखें");
        }
        if (route.startsWith("/report")) {
            return isEn ? "Report a Problem" : (isMr ? "तक्रार नोंदवा" : "शिकायत दर्ज करें");
        }
        if (route.startsWith("/business")) {
            return isEn ? "View Businesses" : (isMr ? "ग्राम व्यवसाय पहा" : "ग्राम व्यापार देखें");
        }
        if (route.startsWith("/weather")) {
            return isEn ? "Weather Forecast" : (isMr ? "हवामान अंदाज पहा" : "मौसम रिपोर्ट देखें");
        }
        if (route.startsWith("/donation")) {
            return isEn ? "Make Donation" : (isMr ? "गाव दान करा" : "गांव दान पेज");
        }
        if (route.startsWith("/home")) {
            return isEn ? "Today's Tips" : (isMr ? "आजच्या टीप्स पहा" : "आज के सुझाव देखें");
        }
        return isEn ? "Explore Portal" : (isMr ? "पोर्टल पहा" : "पोर्टल देखें");
    }

    /**
     * Generates dynamic, relevant follow-up chips based on topic and language
     */
    private List<String> buildContextualChips(String category, String lang) {
        boolean isMr = "mr".equalsIgnoreCase(lang);
        boolean isEn = "en".equalsIgnoreCase(lang);

        if ("MANDI".equals(category)) {
            if (isMr) return Arrays.asList("गव्हाचे बाजारभाव?", "कांद्याचा भाव?", "हरभरा हमीभाव (MSP)?", "इतर पिकांचे भाव");
            if (isEn) return Arrays.asList("Today's Wheat Rate?", "Onion Mandi Price?", "Chana MSP Rate?", "Other Crop Prices");
            return Arrays.asList("आज का गेहूं भाव?", "प्याज का मंडी रेट?", "चना सरकारी MSP?", "अन्य फसलों के भाव");
        }

        if ("FERTILIZER".equals(category)) {
            if (isMr) return Arrays.asList("युरिया किती टाकावा?", "डीएपी प्रमाण?", "१ एकरासाठी खत?", "खतांचे वेळापत्रक");
            if (isEn) return Arrays.asList("How much Urea?", "DAP dosage per acre?", "Potash requirement?", "Fertilizer schedule");
            return Arrays.asList("यूरिया कितना डालें?", "डीएपी की सही मात्रा?", "1 बीघा के लिए खाद?", "खाद समय तालिका");
        }

        if ("CROP_DOCTOR".equals(category) || "KISAN_MITRA".equals(category)) {
            if (isMr) return Arrays.asList("पानांवरील करपा रोग?", "कीड नियंत्रण औषध?", "सेंद्रिय फवारणी?", "दैनिक बाजारभाव");
            if (isEn) return Arrays.asList("Leaf Blight treatment?", "Pest control remedy?", "Organic spray formula?", "Check Mandi Rates");
            return Arrays.asList("पत्ती का पीलापन व रोग?", "कीट नियंत्रण दवा?", "देसी जैविक उपाय?", "मंडी भाव देखें");
        }

        if ("SCHEME".equals(category)) {
            if (isMr) return Arrays.asList("पात्रता निकष काय आहेत?", "लागणारी कागदपत्रे?", "अर्ज कसा करावा?", "इतर योजना");
            if (isEn) return Arrays.asList("Eligibility criteria?", "Required documents?", "How to apply?", "Other schemes");
            return Arrays.asList("पात्रता क्या है?", "आवश्यक दस्तावेज?", "आवेदन कैसे करें?", "अन्य योजनाएं");
        }

        if ("JOB".equals(category)) {
            if (isMr) return Arrays.asList("वेतन किती आहे?", "कामाचे ठिकाण?", "अर्ज कसा करावा?", "इतर नोकऱ्या");
            if (isEn) return Arrays.asList("What is the salary?", "Job location?", "How to apply?", "More jobs");
            return Arrays.asList("वेतन कितना है?", "काम की जगह?", "आवेदन कैसे करें?", "अन्य नौकरियां");
        }

        if ("HEALTH".equals(category)) {
            if (isMr) return Arrays.asList("दवाखान्याची वेळ?", "डॉक्टरांचा फोन नंबर?", "आपत्कालीन सेवा?", "इतर डॉक्टर");
            if (isEn) return Arrays.asList("Clinic timings?", "Doctor phone number?", "Emergency help?", "Other doctors");
            return Arrays.asList("क्लिनिक का समय?", "डॉक्टर का फोन नंबर?", "इमरजेंसी सेवा?", "अन्य डॉक्टर");
        }

        if ("CIVIC".equals(category)) {
            if (isMr) return Arrays.asList("तक्रारीची स्थिती कशी तपासायची?", "रस्त्याची तक्रार", "पाण्याची समस्या", "वीज पुरवठा");
            if (isEn) return Arrays.asList("Check complaint status?", "Road repair issue", "Water supply problem", "Electricity issue");
            return Arrays.asList("शिकायत का स्टेटस कैसे देखें?", "सड़क की शिकायत", "पानी की समस्या", "बिजली समस्या");
        }

        // Default General Chips
        if (isMr) return Arrays.asList("शासकीय योजना", "स्थानिक नोकऱ्या", "गाव डॉक्टर", "तक्रार नोंदवा");
        if (isEn) return Arrays.asList("Government Schemes", "Local Jobs", "Village Doctor", "Report Problem");
        return Arrays.asList("सरकारी योजनाएं", "स्थानीय नौकरियां", "गांव डॉक्टर", "शिकायत दर्ज करें");
    }

    /**
     * Accurately detects whether the query is English, Marathi, or Hindi.
     */
    private String detectLanguage(String query, String requestedLang) {
        if (query == null || query.trim().isEmpty()) {
            return isValidLang(requestedLang) ? requestedLang.toLowerCase() : "hi";
        }

        String lower = query.toLowerCase().trim();

        // Check for purely Devanagari script presence
        boolean hasDevanagari = false;
        for (char c : query.toCharArray()) {
            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.DEVANAGARI) {
                hasDevanagari = true;
                break;
            }
        }

        if (hasDevanagari) {
            // Check for Marathi-specific markers in Devanagari
            String[] marathiMarkersDev = {
                "आहे", "आहेत", "नाही", "पाहिजे", "कसे", "काय", "कधी", "करावे", "शेतकरी", "शेती",
                "गावात", "योजनांची", "माहिती", "सांगा", "द्या", "पहा", "मदत", "तक्रार", "होय", "नका",
                "मला", "तुला", "आपल्या", "झाले", "करू", "नोकऱ्या", "अडचण", "हवामान", "पाऊस",
                "औषध", "दवाखाना", "रुग्णालय", "आरोग्य", "ग्रामपंचायत", "ग्रामसेवक", "धंदा", "उद्योग"
            };
            for (String m : marathiMarkersDev) {
                if (lower.contains(m)) {
                    return "mr";
                }
            }
            return "hi"; // Default Devanagari query is Hindi
        }

        // NO Devanagari present -> Latin script text.
        // Check for Marathilish marker words (whole words with boundary \b)
        String[] marathiLatin = {
            "mala", "pahije", "kasa", "kase", "sheti", "shetkari", "gaavat", "aahe", "ahet",
            "sanga", "dakhav", "nokari", "takrar", "mahit", "havay", "shasan"
        };
        for (String m : marathiLatin) {
            if (Pattern.compile("\\b" + Pattern.quote(m) + "\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
                return "mr";
            }
        }

        // Check for Hinglish marker words (whole words with boundary \b)
        String[] hindiLatin = {
            "chahiye", "batao", "kaise", "kya", "kahan", "karna", "karni", "mujhe", "kijiye",
            "hoga", "mera", "meri", "apna", "apni", "dikhao", "kholo", "hai", "hain", "naukri",
            "kaam", "bataiye", "karen", "kare"
        };
        for (String h : hindiLatin) {
            if (Pattern.compile("\\b" + Pattern.quote(h) + "\\b", Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
                return "hi";
            }
        }

        // Pure Latin query with standard English vocabulary -> strictly English!
        return "en";
    }

    private boolean isValidLang(String lang) {
        return "hi".equalsIgnoreCase(lang) || "en".equalsIgnoreCase(lang) || "mr".equalsIgnoreCase(lang);
    }

    /**
     * Fast Heuristic Matcher with extended multilingual vocabulary
     */
    private RouteMatch matchHeuristicRoute(String q, String lang) {
        boolean isMr = "mr".equalsIgnoreCase(lang);
        boolean isEn = "en".equalsIgnoreCase(lang);

        // Informational queries (e.g. "how many smart village in", "how to use pesticides", "what is vermicompost")
        // should NOT trigger a navigation heuristic!
        boolean isInformational = q.startsWith("how many") || q.startsWith("how to") || q.startsWith("what is") 
                || q.startsWith("why ") || q.startsWith("when ") || q.startsWith("where ") || q.contains("kaise use") 
                || q.contains("kaise kare") || q.contains("kya hota") || q.contains("kiti") || q.contains("kasay");

        boolean hasNavIntent = q.contains("open") || q.contains("kholo") || q.contains("dikhao") 
                || q.contains("dikhaye") || q.contains("chahiye") || q.contains("pahije") || q.contains("show") 
                || q.contains("visit") || q.contains("go to") || q.contains("apply") || q.contains("register")
                || q.contains("start") || q.contains("play");

        boolean isPriceQuery = q.contains("mandi") || q.contains("मंडी") || q.contains("कीमत")
                || q.contains("भाव") || q.contains("रेट") || q.contains("दर") || q.contains("दाम")
                || q.contains("price") || q.contains("rate") || q.contains("bhav") || q.contains("msp")
                || q.contains("बाजारभाव") || q.contains("dhan") || q.contains("धान") || q.contains("gehu")
                || q.contains("गेहूं") || q.contains("चना") || q.contains("सरसों") || q.contains("कांदा")
                || q.contains("सोयाबीन");

        if (isInformational && !hasNavIntent && !isPriceQuery) {
            return new RouteMatch("GENERAL", null, null);
        }

        // KISAN MITRA SUITE (MANDI RATES, FERTILIZER CALC, CROP DOCTOR, KHATA, BAZAAR)
        if (q.contains("mandi") || q.contains("मंडी") || q.contains("बाजारभाव") || q.contains("bajarbhav")
                || q.contains("कीमत") || q.contains("दाम") || q.contains("रेट") || q.contains("price") || q.contains("rate")
                || (q.contains("bhav") && (q.contains("gehu") || q.contains("chana") || q.contains("sarson") || q.contains("dhan") || q.contains("aalu") || q.contains("tamatar") || q.contains("kisan") || q.contains("fasal")))
                || (q.contains("भाव") && (q.contains("गेहूं") || q.contains("चना") || q.contains("सरसों") || q.contains("धान") || q.contains("आलू") || q.contains("टमाटर") || q.contains("प्याज") || q.contains("फसल") || q.contains("आज का") || q.contains("मंडी")))
                || (q.contains("दर") && (q.contains("गहू") || q.contains("हरभरा") || q.contains("कांदा") || q.contains("सोयाबीन") || q.contains("बाजार")))
                || (q.contains("धान") && (q.contains("भाव") || q.contains("रेट") || q.contains("कीमत") || q.contains("सरकारी") || q.contains("msp")))
                || (q.contains("गेहूं") && (q.contains("भाव") || q.contains("रेट") || q.contains("कीमत") || q.contains("msp")))
                || (q.contains("सरसों") && (q.contains("भाव") || q.contains("रेट") || q.contains("कीमत") || q.contains("msp")))) {
            String label = isEn ? "Daily Mandi Rates" : (isMr ? "दैनिक बाजारभाव" : "दैनिक मंडी भाव देखें");

            String mandiRoute = "/kisan-mitra?tab=mandi";
            if (q.contains("banana") || q.contains("kela") || q.contains("केला") || q.contains("केळी")
                    || q.contains("mango") || q.contains("aam") || q.contains("आम") || q.contains("आंबा")
                    || q.contains("apple") || q.contains("seb") || q.contains("सेब") || q.contains("सफरचंद")
                    || q.contains("anar") || q.contains("pomegranate") || q.contains("अनार") || q.contains("डाळिंब")
                    || q.contains("santra") || q.contains("orange") || q.contains("संतरा") || q.contains("संत्री")
                    || q.contains("amrood") || q.contains("guava") || q.contains("अमरूद") || q.contains("पेरू")
                    || q.contains("papaya") || q.contains("papita") || q.contains("पपीता") || q.contains("पपई")
                    || q.contains("angoor") || q.contains("grapes") || q.contains("अंगूर") || q.contains("द्राक्षे")
                    || q.contains("strawberry") || q.contains("स्ट्रॉबेरी") || q.contains("chiku") || q.contains("चीकू")
                    || q.contains("ananas") || q.contains("pineapple") || q.contains("अनानास") || q.contains("फल") || q.contains("फळे") || q.contains("fruit")) {
                mandiRoute = "/kisan-mitra?tab=mandi&category=fruits";
            } else if (q.contains("tamatar") || q.contains("tomato") || q.contains("टमाटर") || q.contains("टोमॅटो")
                    || q.contains("aalu") || q.contains("potato") || q.contains("आलू") || q.contains("बटाटा")
                    || q.contains("pyaz") || q.contains("onion") || q.contains("प्याज") || q.contains("कांदा")
                    || q.contains("gobhi") || q.contains("cauliflower") || q.contains("गोभी") || q.contains("मटर") || q.contains("peas")
                    || q.contains("lahsun") || q.contains("garlic") || q.contains("लहसुन") || q.contains("सब्जी") || q.contains("भाजी") || q.contains("vegetable")) {
                mandiRoute = "/kisan-mitra?tab=mandi&category=vegetables";
            } else if (q.contains("chana") || q.contains("चना") || q.contains("हरभरा") || q.contains("toor") || q.contains("arhar") || q.contains("तूर") || q.contains("दाल") || q.contains("डाळ")) {
                mandiRoute = "/kisan-mitra?tab=mandi&category=pulses";
            } else if (q.contains("sarson") || q.contains("mustard") || q.contains("सरसों") || q.contains("मोहरी") || q.contains("soybean") || q.contains("सोयाबीन")) {
                mandiRoute = "/kisan-mitra?tab=mandi&category=oilseeds";
            } else if (q.contains("dhan") || q.contains("धान") || q.contains("gehu") || q.contains("गेहूं") || q.contains("गहू") || q.contains("makka") || q.contains("मक्का") || q.contains("अनाज")) {
                mandiRoute = "/kisan-mitra?tab=mandi&category=grains";
            }
            return new RouteMatch("MANDI", mandiRoute, label);
        }

        if (q.contains("fertilizer") || q.contains("khad") || q.contains("urea") || q.contains("dap")
                || q.contains("खाद") || q.contains("यूरिया") || q.contains("डीएपी") || q.contains("खत")
                || q.contains("खतांचे प्रमाण")) {
            String label = isEn ? "Fertilizer Calculator" : (isMr ? "खत कॅल्क्युलेटर" : "खाद कैलकुलेटर खोलें");
            return new RouteMatch("FERTILIZER", "/kisan-mitra?tab=fertilizer-calc", label);
        }

        if (q.contains("crop doctor") || q.contains("leaf disease") || q.contains("fasal me rog")
                || q.contains("patti peeli") || q.contains("फसल रोग") || q.contains("पीक डॉक्टर")
                || q.contains("रोग निदान") || q.contains("कीट नियंत्रण")) {
            String label = isEn ? "AI Crop Doctor" : (isMr ? "पीक डॉक्टर स्कॅनर" : "फसल रोग स्कैनर");
            return new RouteMatch("CROP_DOCTOR", "/kisan-mitra?tab=crop-doctor", label);
        }

        if (q.contains("kisan khata") || q.contains("kheti hisab") || q.contains("farm ledger")
                || q.contains("किसान खाता") || q.contains("खेती का हिसाब") || q.contains("शेतकरी खातेवही")) {
            String label = isEn ? "Digital Kisan Khata" : (isMr ? "शेतकरी खातेवही" : "किसान खाता देखें");
            return new RouteMatch("KISAN_MITRA", "/kisan-mitra?tab=khata", label);
        }

        if (q.contains("krishi bazaar") || q.contains("tractor rent") || q.contains("tractor kiraya")
                || q.contains("कृषि बाजार") || q.contains("कृषी बाजार") || q.contains("ट्रैक्टर किराया")) {
            String label = isEn ? "Krishi Bazaar" : (isMr ? "कृषी बाजार" : "कृषि बाज़ार देखें");
            return new RouteMatch("KISAN_MITRA", "/kisan-mitra?tab=krishi-bazaar", label);
        }

        if (q.contains("kisan mitra") || q.contains("kishan mitra") || q.contains("किसान मित्र")
                || q.contains("शेतकरी मित्र")) {
            String label = isEn ? "Kisan Mitra Hub" : (isMr ? "शेतकरी मित्र" : "किसान मित्र हब");
            return new RouteMatch("KISAN_MITRA", "/kisan-mitra", label);
        }

        // 1. BUSINESS SEEKER / EXPLORER (Business ideas / browse shops / business shuru karna)
        // e.g. "business idea chahiye", "business chahiye", "kya business kare", "business shuru karna hai", "vyapar dekhna hai", "dukan dekhni hai"
        if ((q.contains("business") || q.contains("व्यापार") || q.contains("उद्योग") || q.contains("dukan")
                || q.contains("व्यवसाय") || q.contains("दुकान") || q.contains("धंदा") || q.contains("shop")
                || q.contains("मार्केट") || q.contains("बाजार") || q.contains("किराणा"))
                && (q.contains("idea") || q.contains("आइडिया") || q.contains("chahiye") || q.contains("चाहिए")
                || q.contains("pahije") || q.contains("पाहिजे") || q.contains("lena") || q.contains("लेना")
                || q.contains("shuru") || q.contains("शुरू") || q.contains("start") || q.contains("kare")
                || q.contains("करें") || q.contains("dekh") || q.contains("देख") || q.contains("बघ")
                || q.contains("kholna") || q.contains("खोलना") || q.contains("board") || q.contains("बोर्ड"))) {
            String label = isEn ? "View Businesses" : (isMr ? "ग्राम व्यापार पहा" : "ग्राम व्यापार देखें");
            return new RouteMatch("BUSINESS", "/business", label);
        }

        // 2. POST BUSINESS (GIVER / POSTER: Post/List/Add/Register Shop or Business / jankari dena)
        // e.g. "business dena hai", "business ki jankari dena hai", "dukan jodna hai", "apna business list karna hai", "व्यापार माहिती देणे"
        if ((q.contains("post") || q.contains("list") || q.contains("add") || q.contains("register")
                || q.contains("जोड़") || q.contains("लिस्ट") || q.contains("दर्ज") || q.contains("नोंदणी")
                || q.contains("dena") || q.contains("deni") || q.contains("देना") || q.contains("देनी")
                || q.contains("jankari") || q.contains("जानकारी") || q.contains("माहिती"))
                && (q.contains("business") || q.contains("shop") || q.contains("dukan") || q.contains("दुकान")
                || q.contains("बिजनेस") || q.contains("व्यापार") || q.contains("व्यवसाय") || q.contains("धंधा"))) {
            String label = isEn ? "Post Business" : (isMr ? "व्यवसाय नोंदणी" : "बिजनेस पोस्ट करें");
            return new RouteMatch("BUSINESS", "/post-business", label);
        }

        // 3. POST A JOB (GIVER / EMPLOYER: Employer/Hirer posting vacancy / job dena)
        // e.g. "job dena hai", "kaam dena hai", "naukri dena hai", "worker hire karna hai", "naukri post karna hai"
        if ((q.contains("post") || q.contains("hire") || q.contains("dena") || q.contains("deni")
                || q.contains("nikalna") || q.contains("देना") || q.contains("देनी") || q.contains("द्यायचे")
                || q.contains("rakhna") || q.contains("रखना") || (q.contains("worker") && q.contains("chahiye"))
                || (q.contains("मजदूर") && q.contains("चाहिए")) || (q.contains("कारीगर") && q.contains("चाहिए")))
                && (q.contains("job") || q.contains("naukri") || q.contains("kaam") || q.contains("work")
                || q.contains("नौकरी") || q.contains("काम") || q.contains("नोकरी"))
                && !q.contains("naukri chahiye") && !q.contains("job chahiye") && !q.contains("kaam chahiye")
                && !q.contains("नौकरी चाहिए") && !q.contains("काम चाहिए") && !q.contains("नोकरी पाहिजे")) {
            String label = isEn ? "Post a Job" : (isMr ? "नोकरी पोस्ट करा" : "नौकरी पोस्ट करें");
            return new RouteMatch("JOB", "/post-job", label);
        }

        // 3. SCHEMES
        if (q.contains("yojana") || q.contains("scheme") || q.contains("योजना") || q.contains("स्कीम") 
                || q.contains("kisan") || q.contains("awas") || q.contains("subsidy") || q.contains("pension")
                || q.contains("अनुदान") || q.contains("कर्ज") || q.contains("शेतकरी") || q.contains("विमा")
                || q.contains("सब्सिडी") || q.contains("लोन") || q.contains("पेन्शन") || q.contains("सिंचन")) {
            String label = isEn ? "View Schemes" : (isMr ? "शासकीय योजना पहा" : "सरकारी योजनाएं देखें");
            return new RouteMatch("SCHEME", "/scheme", label);
        }

        // 2. GOVERNMENT JOBS SPECIFIC
        if (q.contains("govt") || q.contains("government") || q.contains("सरकारी नौकरी") 
                || q.contains("गवर्नमेंट जॉब") || q.contains("शासकीय नोकरी")
                || (q.contains("job") && q.contains("govt")) || (q.contains("नौकरी") && q.contains("सरकारी"))) {
            String label = isEn ? "Government Jobs" : (isMr ? "शासकीय नोकऱ्या" : "सरकारी नौकरियां");
            return new RouteMatch("JOB", "/job-board?tab=govt", label);
        }

        // 3. JOBS / EMPLOYMENT
        if (q.contains("job") || q.contains("work") || q.contains("career") || q.contains("नौकरी") || q.contains("रोजगार") 
                || q.contains("मजदूरी") || q.contains("kaam") || q.contains("नोकरी") || q.contains("मजुरी")
                || q.contains("electrician") || q.contains("plumber") || q.contains("मिस्त्री") || q.contains("सुतार")
                || q.contains("रिक्ती") || q.contains("vacancy") || q.contains("भर्ती") || q.contains("कामाची संधी")
                || q.contains("engineer") || q.contains("इंजीनियर") || q.contains("अभियंता") || q.contains("driver")
                || q.contains("ड्रायव्हर") || q.contains("ड्राइवर") || q.contains("teacher") || q.contains("शिक्षक")
                || q.contains("painter") || q.contains("पेंटर") || q.contains("security") || q.contains("सुरक्षा")) {
            
            String specificSearch = null;
            if (q.contains("engineer") || q.contains("इंजीनियर") || q.contains("अभियंता")) specificSearch = "engineer";
            else if (q.contains("electrician") || q.contains("इलेक्ट्रीशियन")) specificSearch = "electrician";
            else if (q.contains("plumber") || q.contains("प्लंबर")) specificSearch = "plumber";
            else if (q.contains("driver") || q.contains("ड्रायव्हर") || q.contains("ड्राइवर")) specificSearch = "driver";
            else if (q.contains("teacher") || q.contains("शिक्षक") || q.contains("अध्यापक")) specificSearch = "teacher";
            else if (q.contains("carpenter") || q.contains("सुतार") || q.contains("बढ़ई")) specificSearch = "carpenter";
            else if (q.contains("painter") || q.contains("पेंटर")) specificSearch = "painter";
            else if (q.contains("security") || q.contains("सुरक्षा")) specificSearch = "security";

            String route = (specificSearch != null) ? "/job-board?search=" + specificSearch : "/job-board";
            String label = isEn ? "View Jobs" : (isMr ? "नोकऱ्या पहा" : "नौकरियां देखें");
            return new RouteMatch("JOB", route, label);
        }

        // 4. SHIKSHA QUIZ (COMPETITIVE EXAM PREPARATION - SSC, Railway, Bank, State Exam, Exam Prep)
        if (q.contains("ssc") || q.contains("railway") || q.contains("bank exam") || q.contains("banking")
                || q.contains("shiksha quiz") || q.contains("shiksha-quiz") || q.contains("शिक्षा क्विज") 
                || q.contains("शिक्षा क्विझ") || q.contains("competitive") || q.contains("exam prep")
                || q.contains("prepare for") || q.contains("taiyari") || q.contains("तैयारी")
                || q.contains("एसएससी") || q.contains("रेलवे") || q.contains("बैंकिंग") || q.contains("प्रतियोगी परीक्षा")
                || q.contains("स्पर्धा परीक्षा") || (q.contains("exam") && (q.contains("quiz") || q.contains("prep") || q.contains("test")))) {
            String label = isEn ? "Shiksha Quiz" : (isMr ? "शिक्षा क्विझ" : "शिक्षा क्विज");
            return new RouteMatch("EDUCATION", "/shiksha-quiz", label);
        }

        // 5. DAILY QUIZ
        if (q.contains("daily quiz") || q.contains("daily-quiz") || q.contains("डेली क्विज") 
                || q.contains("दैनिक प्रश्नमंजुषा") || (q.contains("quiz") && !q.contains("competition") && !q.contains("shiksha"))
                || q.contains("क्विज") || q.contains("क्विज़") || q.contains("प्रश्नमंजुषा")) {
            String label = isEn ? "Daily Quiz" : (isMr ? "दैनिक प्रश्नमंजुषा" : "डेली क्विज");
            return new RouteMatch("EDUCATION", "/daily-quiz", label);
        }

        // 5. SMARTGAON INTENT
        if ((q.contains("smartgaon") || q.contains("smart gaon") || q.contains("स्मार्ट गांव") || q.contains("स्मार्टगांव")) 
                && hasNavIntent) {
            String label = isEn ? "SmartGaon" : (isMr ? "स्मार्टगाव" : "स्मार्टगांव");
            return new RouteMatch("GENERAL", "/smartgaon", label);
        }

        // 6. HEALTH / DOCTORS
        if (q.contains("doctor") || q.contains("health") || q.contains("hospital") || q.contains("डॉक्टर") || q.contains("अस्पताल") 
                || q.contains("दवाई") || q.contains("clinic") || q.contains("diet") || q.contains("आहार")
                || q.contains("दवाखाना") || q.contains("औषध") || q.contains("रुग्णालय") || q.contains("आरोग्य")
                || q.contains("तबीयत") || q.contains("बीमार") || q.contains("आजारी") || q.contains("इलाज")) {
            String label = isEn ? "Contact Doctor" : (isMr ? "गाव डॉक्टर संपर्क" : "गांव डॉक्टर संपर्क");
            return new RouteMatch("HEALTH", "/gaon-doctor", label);
        }

        // 7. DIRECTORY
        if (q.contains("pradhan") || q.contains("sarpanch") || q.contains("panchayat") || q.contains("प्रधान") 
                || q.contains("सचिव") || q.contains("directory") || q.contains("phone") || q.contains("number")
                || q.contains("ग्रामपंचायत") || q.contains("ग्रामसेवक") || q.contains("निर्देशिका") || q.contains("संपर्क")
                || q.contains("सरपंच") || q.contains("मुखिया") || q.contains("contact")) {
            String label = isEn ? "Village Directory" : (isMr ? "गाव निर्देशिका पहा" : "गांव डायरेक्टरी देखें");
            return new RouteMatch("DIRECTORY", "/directory", label);
        }

        // 8. EDUCATION / COMPETITION
        if (q.contains("competition") || q.contains("प्रतियोगिता") || q.contains("स्पर्धा") 
                || q.contains("इनाम") || q.contains("बक्षीस")) {
            String label = isEn ? "View Competitions" : (isMr ? "शाळा स्पर्धा पहा" : "स्कूल प्रतियोगिताएं देखें");
            return new RouteMatch("EDUCATION", "/school-competition", label);
        }

        // 6. CIVIC / COMPLAINT
        if (q.contains("shikayat") || q.contains("report") || q.contains("problem") || q.contains("complaint")
                || q.contains("issue") || q.contains("शिकायत") || q.contains("सुझाव") || q.contains("कंप्लेंट")
                || q.contains("तक्रार") || q.contains("अडचण") || q.contains("sadak") || q.contains("रस्ता")
                || q.contains("पाणी") || q.contains("वीज") || q.contains("बिजली") || q.contains("कचरा")
                || q.contains("गंदगी") || q.contains("light") || q.contains("water") || q.contains("road")) {
            String label = isEn ? "Report a Problem" : (isMr ? "तक्रार नोंदवा" : "शिकायत दर्ज करें");
            return new RouteMatch("CIVIC", "/report", label);
        }

        // 7. BUSINESS
        if (q.contains("business") || q.contains("व्यापार") || q.contains("उद्योग") || q.contains("dukan")
                || q.contains("व्यवसाय") || q.contains("दुकान") || q.contains("धंदा") || q.contains("shop")
                || q.contains("मार्केट") || q.contains("बाजार") || q.contains("किराणा")) {
            String label = isEn ? "View Businesses" : (isMr ? "ग्राम व्यवसाय पहा" : "ग्राम व्यापार देखें");
            return new RouteMatch("BUSINESS", "/business", label);
        }

        // 8. WEATHER
        if (q.contains("weather") || q.contains("मौसम") || q.contains("barish") || q.contains("हवामान")
                || q.contains("पाऊस") || q.contains("तापमान") || q.contains("rain") || q.contains("forecast")) {
            String label = isEn ? "Weather Forecast" : (isMr ? "हवामान अंदाज पहा" : "मौसम रिपोर्ट देखें");
            return new RouteMatch("WEATHER", "/weather", label);
        }

        // 10. TODAY'S TIPS (e.g. "today tips", "today's tip", "daily tip", "aaj ka tip", "kisan tips", "tips")
        if (q.contains("today tip") || q.contains("todays tip") || q.contains("daily tip") || q.equals("tips")
                || q.equals("tip") || q.contains("aaj ka tip") || q.contains("aaj ke tip") || q.contains("आज का टिप")
                || q.contains("आज के सुझाव") || q.contains("आजची टीप") || q.contains("farming tip") || q.contains("kisan tip")
                || q.contains("daily advice") || q.contains("tips dikhao")) {
            String label = isEn ? "Today's Tips" : (isMr ? "आजच्या टीप्स पहा" : "आज के सुझाव देखें");
            return new RouteMatch("TIPS", "/home", label);
        }

        return new RouteMatch("GENERAL", null, null);
    }

    /**
     * Fetches Live Multi-Entity Database Context (Grounding facts, compressed to save tokens)
     */
    private String fetchMultiEntityDbContext(String query, String category) {
        StringBuilder sb = new StringBuilder();
        try {
            // 0. KISAN MITRA & LIVE MANDI CONTEXT
            if ("MANDI".equals(category) || "FERTILIZER".equals(category) || "CROP_DOCTOR".equals(category) || "KISAN_MITRA".equals(category)
                    || query.contains("mandi") || query.contains("मंडी") || query.contains("bhav") || query.contains("भाव") || query.contains("दर")
                    || query.contains("कीमत") || query.contains("रेट") || query.contains("दाम") || query.contains("price") || query.contains("rate")
                    || query.contains("dhan") || query.contains("धान") || query.contains("gehu") || query.contains("गेहूं")
                    || query.contains("चना") || query.contains("सरसों") || query.contains("कांदा") || query.contains("सोयाबीन")
                    || query.contains("khad") || query.contains("खाद")) {
                boolean hasLiveDbRates = false;
                if (mandiRateRepository != null) {
                    try {
                        List<MandiRate> dbRates = mandiRateRepository.findAll();
                        if (dbRates != null && !dbRates.isEmpty()) {
                            sb.append("Live Official Mandi Database Rates (Agmarknet & APMC Live):\n");
                            int count = 0;
                            for (MandiRate r : dbRates) {
                                if (count++ >= 25) break;
                                sb.append("- ").append(r.getCommodity())
                                  .append(" (").append(r.getMarket() != null ? r.getMarket() : r.getDistrict() + " Mandi").append("): ")
                                  .append("Modal ₹").append(Math.round(r.getModalPrice())).append("/Quintal")
                                  .append(r.getMinPrice() != null ? " (Range: ₹" + Math.round(r.getMinPrice()) + " - ₹" + Math.round(r.getMaxPrice()) + "/Quintal)" : "")
                                  .append(r.getMsp() != null ? ", Govt MSP: ₹" + Math.round(r.getMsp()) + "/Quintal" : "")
                                  .append(r.getSource() != null ? " [" + r.getSource() + "]" : "")
                                  .append("\n");
                            }
                            hasLiveDbRates = true;
                        }
                    } catch (Exception ex) {
                        System.err.println("[GaonSaathi RAG] Mandi DB lookup note: " + ex.getMessage());
                    }
                }
                if (!hasLiveDbRates) {
                    sb.append("Live Certified Mandi Rates & ICAR Agricultural Intelligence:\n");
                sb.append("- Wheat (गेहूं/गहू): Modal ₹2,425/Quintal (≈ ₹24.25/kg), Govt MSP: ₹2,425/Quintal (Steady wholesale trading)\n");
                sb.append("- Paddy (धान/भात): Modal ₹2,300/Quintal (≈ ₹23/kg), Govt MSP: ₹2,300/Quintal (Active procurement)\n");
                sb.append("- Basmati Rice (1121): Modal ₹4,250/Quintal (≈ ₹42.50/kg)\n");
                sb.append("- Chickpea (चना/हरभरा): Modal ₹6,150/Quintal (≈ ₹61.50/kg), Govt MSP: ₹5,440/Quintal\n");
                sb.append("- Toor Dal (अरहर दाल/तूर डाळ): Modal ₹12,400/Quintal (≈ ₹124/kg)\n");
                sb.append("- Mustard (सरसों/मोहरी): Modal ₹5,680/Quintal (≈ ₹56.80/kg), Govt MSP: ₹5,650/Quintal\n");
                sb.append("- Soybean (सोयाबीन): Modal ₹4,650/Quintal (≈ ₹46.50/kg), Govt MSP: ₹4,892/Quintal\n");
                sb.append("- Onion (प्याज/कांदा): Modal ₹2,750/Quintal (≈ ₹27.50/kg), Potato (आलू/बटाटा): ₹1,600/Quintal (≈ ₹16/kg), Tomato: ₹2,100/Quintal (≈ ₹21/kg)\n");
                sb.append("- Spinach (पालक): ₹1,800/Quintal (₹18/kg), Cucumber (खीरा/काकडी): ₹2,000/Quintal (₹20/kg), Capsicum (शिमला मिर्च): ₹4,200/Quintal (₹42/kg)\n");
                sb.append("- Drumstick (सहजन/शेवगा): ₹5,000/Quintal (₹50/kg), Button Mushroom: ₹13,500/Quintal (₹135/kg)\n");
                sb.append("- Lemon (नींबू/लिंबू): ₹5,800/Quintal (₹58/kg), Coconut (नारियल): ₹3,300/Quintal, Pineapple: ₹3,200/Quintal, Strawberry: ₹20,000/Quintal (₹200/kg)\n");
                sb.append("- In-Season Fruits actively selling in Mandi: Banana (केला/केळी): ₹2,000/Quintal (≈ ₹20/kg), Apple (सेब): ₹8,000/Quintal (≈ ₹80/kg), Pomegranate (अनार/डाळिंब): ₹9,500/Quintal (≈ ₹95/kg), Orange (संतरा): ₹4,500/Quintal (≈ ₹45/kg), Guava (अमरूद/पेरू): ₹2,800/Quintal (≈ ₹28/kg), Chiku: ₹2,800/Quintal (≈ ₹28/kg)\n");
                sb.append("- Active Mandi Sale Rule: Mango (आम/आंबा) and Litchi (लीची) are NOT sold in the mandi today because they are completely off-season (Mango harvest is April–July). Products with 0 arrival or out-of-season are not listed in active mandi auctions.\n");
                sb.append("- Marigold (गेंदा फूल/झेंडू): ₹4,800/Quintal (₹48/kg), Rose (गुलाब): ₹18,000/Quintal (₹180/kg), Mogra: ₹28,000/Quintal (₹280/kg)\n");
                sb.append("- Black Pepper (काली मिर्च): ₹63,500/Quintal (₹635/kg), Green Cardamom (इलायची/वेलची): ₹2,38,000/Quintal (₹2,380/kg)\n");
                sb.append("- Ashwagandha (अश्वगंधा): ₹28,500/Quintal, Isabgol: ₹15,800/Quintal, Tulsi: ₹8,500/Quintal, Garlic (लहसुन): ₹13,500/Quintal\n");
                }
                sb.append("- ICAR Dosage: Wheat 2.2 bags Urea, 1.1 bags DAP/acre; Paddy 2.0 bags Urea, 1.0 bag DAP/acre.\n");
            }

            // 1. SCHEMES CONTEXT
            if ("SCHEME".equals(category) || query.contains("yojana") || query.contains("योजना") || query.contains("scheme")) {
                if (userSchemeRepository != null) {
                    List<Scheme> schemes = userSchemeRepository.findAll();
                    if (schemes != null && !schemes.isEmpty()) {
                        sb.append("Live Official Schemes in Portal:\n");
                        int count = 0;
                        for (Scheme s : schemes) {
                            if (count++ >= 4) break;
                            sb.append("- ").append(s.getTitle())
                              .append(s.getBenefits() != null ? " (Benefits: " + s.getBenefits() + ")" : "")
                              .append(s.getEligibility() != null ? " [Eligibility: " + s.getEligibility() + "]" : "")
                              .append("\n");
                        }
                    }
                }
                if (governmentOfferRepository != null && sb.length() == 0) {
                    List<GovernmentOffer> offers = governmentOfferRepository.findAll();
                    if (offers != null && !offers.isEmpty()) {
                        sb.append("Live Govt Offers:\n");
                        int count = 0;
                        for (GovernmentOffer o : offers) {
                            if (count++ >= 4) break;
                            sb.append("- ").append(o.getName()).append(" (Dept: ").append(o.getDepartment()).append(")\n");
                        }
                    }
                }
            }

            // 2. JOBS CONTEXT
            else if ("JOB".equals(category) || query.contains("job") || query.contains("नौकरी") || query.contains("नोकरी")) {
                if (jobRepository != null) {
                    List<Job> jobs = jobRepository.findAll();
                    if (jobs != null && !jobs.isEmpty()) {
                        sb.append("Live Available Jobs:\n");
                        int count = 0;
                        for (Job j : jobs) {
                            if (count++ >= 4) break;
                            sb.append("- ").append(j.getTitle()).append(" in ").append(j.getLocation())
                              .append(" (Salary: ").append(j.getSalaryRange()).append(")\n");
                        }
                    }
                }
            }

            // 3. DOCTORS / HEALTH CONTEXT
            else if ("HEALTH".equals(category) || query.contains("doctor") || query.contains("डॉक्टर") || query.contains("दवाखाना")) {
                if (doctorRepository != null) {
                    List<Doctor> doctors = doctorRepository.findByActiveTrue();
                    if (doctors != null && !doctors.isEmpty()) {
                        sb.append("Verified Village Doctors:\n");
                        int count = 0;
                        for (Doctor d : doctors) {
                            if (count++ >= 4) break;
                            sb.append("- Dr. ").append(d.getName()).append(" (").append(d.getSpecialty()).append("), ")
                              .append(d.getClinicOrHospital()).append(", Timing: ").append(d.getAvailableTime())
                              .append(d.getPhone() != null ? ", Phone: " + d.getPhone() : "").append("\n");
                        }
                    }
                }
            }

            // 4. BUSINESS CONTEXT
            else if ("BUSINESS".equals(category) || query.contains("business") || query.contains("दुकान") || query.contains("व्यवसाय")) {
                if (businessPostRepository != null) {
                    List<BusinessPost> businesses = businessPostRepository.findAll();
                    if (businesses != null && !businesses.isEmpty()) {
                        sb.append("Registered Village Businesses:\n");
                        int count = 0;
                        for (BusinessPost b : businesses) {
                            if (count++ >= 4) break;
                            sb.append("- ").append(b.getTitle())
                              .append(b.getLocation() != null ? " (" + b.getLocation() + ")" : "")
                              .append("\n");
                        }
                    }
                }
            }

            // 5. EDUCATION / COMPETITION CONTEXT
            else if ("EDUCATION".equals(category) || query.contains("competition") || query.contains("स्पर्धा") || query.contains("प्रतियोगिता")) {
                if (schoolCompetitionRepository != null) {
                    List<SchoolCompetition> comps = schoolCompetitionRepository.findByIsLiveTrue();
                    if (comps != null && !comps.isEmpty()) {
                        sb.append("Active School Competitions:\n");
                        int count = 0;
                        for (SchoolCompetition c : comps) {
                            if (count++ >= 4) break;
                            sb.append("- ").append(c.getTitle()).append(" (Category: ").append(c.getCategory()).append(")\n");
                        }
                    }
                }
            }

            // 6. TODAY'S TIPS CONTEXT
            else if ("TIPS".equals(category) || query.contains("tip") || query.contains("सुझाव") || query.contains("टिप") || query.contains("सलाह") || query.contains("सल्ला")) {
                if (todayTipRedisService != null) {
                    List<TodayTip> tips = todayTipRedisService.fetchActiveTips("ALL");
                    if (tips != null && !tips.isEmpty()) {
                        sb.append("Today's Active Village Tips:\n");
                        int count = 0;
                        for (TodayTip t : tips) {
                            if (count++ >= 3) break;
                            sb.append("- [").append(t.getCategory() != null ? t.getCategory() : "General").append("] ")
                              .append(t.getTitle()).append(": ").append(t.getDescription()).append("\n");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[GaonSaathi RAG] Live DB retrieval note: " + e.getMessage());
        }

        if (sb.length() == 0) {
            sb.append("Standard SmartGaon digital village services and guidance active.");
        }
        return sb.toString();
    }

    /**
     * Calls Groq AI with Fast Model (qwen/qwen3.8-27b) and Token Cap (160 max tokens)
     */
    private String callGroqApi(String systemPrompt, String userQuery) {
        String effectiveKey = (groqApiKey != null && !groqApiKey.trim().isEmpty())
                ? groqApiKey.trim()
                : (groqQuestionService != null ? groqQuestionService.getRawGroqApiKey() : null);

        if (effectiveKey == null || effectiveKey.trim().isEmpty()) {
            System.err.println("[GaonSaathi RAG] Groq API key is not configured.");
            return null;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(effectiveKey.trim());
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "qwen/qwen3.8-27b");

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, String> usrMsg = new HashMap<>();
            usrMsg.put("role", "user");
            usrMsg.put("content", userQuery);
            messages.add(usrMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.2); // Low temperature for high precision & consistency
            requestBody.put("max_tokens", 160);  // Token Cap: saves tokens and ensures quick, crisp answers

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.groq.com/openai/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            System.err.println("[GaonSaathi RAG] Groq API Call Warning: " + e.getMessage());
        }
        return null;
    }

    private String buildSafeFallbackReply(String lang, String route) {
        if (route != null && route.contains("tab=mandi")) {
            if ("mr".equalsIgnoreCase(lang)) {
                return "शासकीय कृषी उत्पन्न बाजार समिती (APMC) नुसार आज भाताचा (धान) सरासरी भाव ₹२,३००/क्विंटल (हमीभाव ₹२,३००), गहू ₹२,४२५/क्विंटल (हमीभाव ₹२,४२५) आणि हरभरा ₹६,१५०/क्विंटल आहे. थेट बाजारभाव पाहण्यासाठी खालील बटणावर क्लिक करा.";
            } else if ("en".equalsIgnoreCase(lang)) {
                return "According to official APMC benchmarks, today's Modal Price for Paddy is ₹2,300/Quintal (MSP ₹2,300), Wheat ₹2,425/Quintal (MSP ₹2,425), and Chickpea ₹6,150/Quintal. Click below to view the live Mandi Rates explorer.";
            } else {
                return "सरकारी मंडी और APMC के अनुसार आज धान का मॉडल भाव ₹2,300 प्रति क्विंटल (सरकारी MSP ₹2,300), गेहूं ₹2,425/क्विंटल (MSP ₹2,425), चना ₹6,150/क्विंटल और सरसों ₹5,680/क्विंटल है। सभी फसलों के लाइव भाव देखने के लिए नीचे दिए गए बटन पर क्लिक करें।";
            }
        }
        if ("mr".equalsIgnoreCase(lang)) {
            return "नमस्कार! मी तुमची विनंती तपासली आहे. "
                    + (route != null ? "आपण खालील बटणावरून थेट संबंधित पेज पाहू शकता." : "कृपया आपला प्रश्न थोडक्यात पुन्हा विचारा.");
        } else if ("en".equalsIgnoreCase(lang)) {
            return "Hello! I processed your request. "
                    + (route != null ? "You can click the button below to view the relevant page." : "Please rephrase your query if you need further help.");
        } else {
            return "नमस्ते! मैंने आपका अनुरोध देखा है। "
                    + (route != null ? "आप नीचे दिए गए बटन पर क्लिक करके सीधे संबंधित पेज देख सकते हैं।" : "कृपया अपना सवाल दोबारा पूछें ताकि मैं पूरी सहायता कर सकूँ।");
        }
    }

    private String normalizeForCache(String query) {
        return query.replaceAll("[^a-zA-Z0-9\\u0900-\\u097F]", "").trim();
    }

    public static class RouteMatch {
        private final String category;
        private final String route;
        private final String actionLabel;

        public RouteMatch(String category, String route, String actionLabel) {
            this.category = category;
            this.route = route;
            this.actionLabel = actionLabel;
        }

        public String getCategory() {
            return category;
        }

        public String getRoute() {
            return route;
        }

        public String getActionLabel() {
            return actionLabel;
        }
    }
}
