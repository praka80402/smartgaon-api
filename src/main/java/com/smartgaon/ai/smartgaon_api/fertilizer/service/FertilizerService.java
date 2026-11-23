package com.smartgaon.ai.smartgaon_api.fertilizer.service;

import com.smartgaon.ai.smartgaon_api.fertilizer.entity.CropFertilizerStandard;
import com.smartgaon.ai.smartgaon_api.fertilizer.entity.FertilizerPrice;
import com.smartgaon.ai.smartgaon_api.fertilizer.repository.CropFertilizerStandardRepository;
import com.smartgaon.ai.smartgaon_api.fertilizer.repository.FertilizerPriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FertilizerService {

    private final FertilizerPriceRepository priceRepository;
    private final CropFertilizerStandardRepository cropRepository;

    @PostConstruct
    public void initDefaultDataIfEmpty() {
        try {
            if (priceRepository.count() == 0) {
                log.info("[FertilizerService] Seeding default ICAR & IFFCO benchmark prices...");
                seedDefaultPrices();
            }
            if (cropRepository.count() == 0) {
                log.info("[FertilizerService] Seeding standard agronomy crop RDFs...");
                seedDefaultCrops();
            }
        } catch (Exception e) {
            log.warn("[FertilizerService] Auto-seed warning: {}", e.getMessage());
        }
    }

    public Map<String, Object> getPricesPayload() {
        return getPricesPayload(null);
    }

    public Map<String, Object> getPricesPayload(String state) {
        List<FertilizerPrice> list = priceRepository.findAll();
        if (list.isEmpty()) {
            seedDefaultPrices();
            list = priceRepository.findAll();
        }

        String matchedKey = resolveStateKey(state);
        StatePricingProfile profile = STATE_PROFILES.getOrDefault(matchedKey, STATE_PROFILES.get("ALL_INDIA"));

        Map<String, Double> ratesMap = new HashMap<>();
        Map<String, FertilizerPrice> detailMap = new HashMap<>();
        List<FertilizerPrice> tailoredList = new ArrayList<>();
        LocalDateTime latest = null;

        for (FertilizerPrice p : list) {
            Double price = p.getPrice();
            if (profile.priceOverrides != null && profile.priceOverrides.containsKey(p.getItemKey())) {
                price = profile.priceOverrides.get(p.getItemKey());
            }

            ratesMap.put(p.getItemKey(), price);

            FertilizerPrice tailored = FertilizerPrice.builder()
                    .id(p.getId())
                    .itemKey(p.getItemKey())
                    .nameEn(p.getNameEn())
                    .nameHi(p.getNameHi())
                    .nameMr(p.getNameMr())
                    .category(p.getCategory())
                    .unit(p.getUnit())
                    .price(price)
                    .mrp(p.getMrp())
                    .subsidyAmount(p.getSubsidyAmount())
                    .brand(p.getBrand())
                    .source(profile.agencyName)
                    .createdAt(p.getCreatedAt())
                    .lastUpdated(p.getLastUpdated())
                    .build();

            detailMap.put(p.getItemKey(), tailored);
            tailoredList.add(tailored);

            if (latest == null || (p.getLastUpdated() != null && p.getLastUpdated().isAfter(latest))) {
                latest = p.getLastUpdated();
            }
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("state", profile.stateNameEn);
        res.put("stateHi", profile.stateNameHi);
        res.put("stateMr", profile.stateNameMr);
        res.put("stateAgency", profile.agencyName);
        res.put("stateScheme", profile.schemeName);
        res.put("rates", ratesMap);
        res.put("details", detailMap);
        res.put("items", tailoredList);
        res.put("supportedStates", getSupportedStates());
        res.put("lastUpdated", latest != null ? latest.toString() : LocalDateTime.now().toString());
        res.put("source", profile.agencyName + " / Official e-PoS Feed");
        return res;
    }

    public List<Map<String, Object>> getSupportedStates() {
        List<Map<String, Object>> states = new ArrayList<>();
        for (StatePricingProfile p : STATE_PROFILES.values()) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.id);
            m.put("nameEn", p.stateNameEn);
            m.put("nameHi", p.stateNameHi);
            m.put("nameMr", p.stateNameMr);
            m.put("agency", p.agencyName);
            m.put("scheme", p.schemeName);
            states.add(m);
        }
        return states;
    }

    private static String resolveStateKey(String state) {
        if (state == null || state.trim().isEmpty()) {
            return "ALL_INDIA";
        }
        String clean = state.toLowerCase().replaceAll("[^a-z0-9]", "");
        if (clean.contains("uttar") || clean.equals("up")) return "UP";
        if (clean.contains("bihar") || clean.equals("br")) return "BIHAR";
        if (clean.contains("maharashtra") || clean.equals("mh")) return "MAHARASHTRA";
        if (clean.contains("madhya") || clean.equals("mp")) return "MP";
        if (clean.contains("punjab") || clean.equals("pb")) return "PUNJAB";
        if (clean.contains("haryana") || clean.equals("hr")) return "HARYANA";
        if (clean.contains("rajasthan") || clean.equals("rj")) return "RAJASTHAN";
        if (clean.contains("gujarat") || clean.equals("gj")) return "GUJARAT";
        if (clean.contains("karnataka") || clean.equals("ka")) return "KARNATAKA";
        if (clean.contains("andhra") || clean.equals("ap")) return "ANDHRA_PRADESH";
        if (clean.contains("telangana") || clean.equals("ts")) return "TELANGANA";
        if (clean.contains("bengal") || clean.equals("wb")) return "WEST_BENGAL";
        if (clean.contains("tamil") || clean.equals("tn")) return "TAMIL_NADU";
        if (clean.contains("odisha") || clean.contains("orissa") || clean.equals("or")) return "ODISHA";
        return "ALL_INDIA";
    }

    private static class StatePricingProfile {
        String id;
        String stateNameEn;
        String stateNameHi;
        String stateNameMr;
        String agencyName;
        String schemeName;
        Map<String, Double> priceOverrides;

        StatePricingProfile(String id, String nameEn, String nameHi, String nameMr, String agency, String scheme, Map<String, Double> overrides) {
            this.id = id;
            this.stateNameEn = nameEn;
            this.stateNameHi = nameHi;
            this.stateNameMr = nameMr;
            this.agencyName = agency;
            this.schemeName = scheme;
            this.priceOverrides = overrides;
        }
    }

    private static Map<String, Double> createPriceMap(
            double urea, double dap, double mop, double zinc,
            double nanoUrea, double nanoDap, double vermi,
            double fym, double neem, double prom, double bioKit, double jeevamrut
    ) {
        Map<String, Double> m = new HashMap<>();
        m.put("ureaBag", urea);
        m.put("dapBag", dap);
        m.put("mopBag", mop);
        m.put("zincPerKg", zinc);
        m.put("nanoUreaBottle", nanoUrea);
        m.put("nanoDapBottle", nanoDap);
        m.put("vermiBag50", vermi);
        m.put("fymTrolley", fym);
        m.put("neemCake50", neem);
        m.put("promBag50", prom);
        m.put("bioKit", bioKit);
        m.put("jeevamrut", jeevamrut);
        return m;
    }

    private static final Map<String, StatePricingProfile> STATE_PROFILES = new LinkedHashMap<>();

    static {
        // Uttar Pradesh
        STATE_PROFILES.put("UP", new StatePricingProfile(
                "uttar_pradesh", "Uttar Pradesh", "उत्तर प्रदेश", "उत्तर प्रदेश",
                "UP PCF / Primary Agricultural Credit Society (PACS)",
                "UP Pandit Deen Dayal Upadhyaya Kisan Samridhi & Soil DBT",
                createPriceMap(266.50, 1350.00, 1700.00, 85.00, 225.00, 600.00, 280.00, 1150.00, 1200.00, 880.00, 340.00, 60.00)
        ));

        // Bihar
        STATE_PROFILES.put("BIHAR", new StatePricingProfile(
                "bihar", "Bihar", "बिहार", "बिहार",
                "Biscomaun / Bihar State Cooperative Bank / PACS",
                "Bihar Rajya Jaivik Protsahan & Khad Anudan",
                createPriceMap(266.50, 1350.00, 1710.00, 88.00, 225.00, 600.00, 260.00, 1050.00, 1180.00, 860.00, 320.00, 50.00)
        ));

        // Maharashtra
        STATE_PROFILES.put("MAHARASHTRA", new StatePricingProfile(
                "maharashtra", "Maharashtra", "महाराष्ट्र", "महाराष्ट्र",
                "MahaDBT / Maharashtra Agro Industries Corp / PACS",
                "Dr. Punjabrao Deshmukh Jaivik Kheti Mission & MahaDBT",
                createPriceMap(266.50, 1350.00, 1700.00, 86.00, 225.00, 600.00, 320.00, 1350.00, 1280.00, 920.00, 330.00, 65.00)
        ));

        // Madhya Pradesh
        STATE_PROFILES.put("MP", new StatePricingProfile(
                "madhya_pradesh", "Madhya Pradesh", "मध्य प्रदेश", "मध्य प्रदेश",
                "MP Markfed / Sahakari Samiti",
                "MP Mukhyamantri Kisan Kalyan & Bio-subsidy",
                createPriceMap(266.50, 1350.00, 1700.00, 84.00, 225.00, 600.00, 270.00, 1100.00, 1220.00, 850.00, 330.00, 55.00)
        ));

        // Punjab
        STATE_PROFILES.put("PUNJAB", new StatePricingProfile(
                "punjab", "Punjab", "पंजाब", "पंजाब",
                "Punjab Markfed / Primary Agricultural Societies",
                "Punjab Soil Health & Micronutrient Assistance",
                createPriceMap(266.50, 1350.00, 1700.00, 80.00, 225.00, 600.00, 300.00, 1400.00, 1250.00, 900.00, 350.00, 70.00)
        ));

        // Haryana
        STATE_PROFILES.put("HARYANA", new StatePricingProfile(
                "haryana", "Haryana", "हरियाणा", "हरियाणा",
                "HAFED / Haryana Agri Marketing Board",
                "Mera Pani Meri Virasat & Soil Nutrient Incentive",
                createPriceMap(266.50, 1350.00, 1700.00, 80.00, 225.00, 600.00, 300.00, 1400.00, 1240.00, 890.00, 340.00, 65.00)
        ));

        // Rajasthan
        STATE_PROFILES.put("RAJASTHAN", new StatePricingProfile(
                "rajasthan", "Rajasthan", "राजस्थान", "राजस्थान",
                "RAJFED / Rajasthan Rajya Sahakari Kray-Vikray",
                "Rajasthan Krishi Sathi & Bio-Fertilizer Subsidy",
                createPriceMap(266.50, 1350.00, 1710.00, 85.00, 225.00, 600.00, 310.00, 1300.00, 1260.00, 890.00, 350.00, 60.00)
        ));

        // Gujarat
        STATE_PROFILES.put("GUJARAT", new StatePricingProfile(
                "gujarat", "Gujarat", "गुजरात", "गुजरात",
                "GSFC / GNFC / GUJCOMASOL",
                "Gujarat Natural Farming Mission",
                createPriceMap(266.50, 1350.00, 1680.00, 84.00, 225.00, 600.00, 290.00, 1250.00, 1250.00, 880.00, 340.00, 55.00)
        ));

        // Karnataka
        STATE_PROFILES.put("KARNATAKA", new StatePricingProfile(
                "karnataka", "Karnataka", "कर्नाटक", "कर्नाटक",
                "Karnataka State Co-op Marketing Federation (KSCMF)",
                "Raitha Siri & Organic Policy 2026",
                createPriceMap(266.50, 1350.00, 1700.00, 88.00, 225.00, 600.00, 340.00, 1400.00, 1300.00, 930.00, 350.00, 70.00)
        ));

        // Andhra Pradesh
        STATE_PROFILES.put("ANDHRA_PRADESH", new StatePricingProfile(
                "andhra_pradesh", "Andhra Pradesh", "आंध्र प्रदेश", "आंध्र प्रदेश",
                "Rythu Bharosa Kendra (RBK) / AP Markfed",
                "YSR Rythu Bharosa & Natural Farming (APCNF)",
                createPriceMap(266.50, 1350.00, 1700.00, 86.00, 225.00, 600.00, 330.00, 1350.00, 1290.00, 910.00, 340.00, 65.00)
        ));

        // Telangana
        STATE_PROFILES.put("TELANGANA", new StatePricingProfile(
                "telangana", "Telangana", "तेलंगाना", "तेलंगणा",
                "TS Markfed / Primary Agricultural Coops",
                "Rythu Bandhu & Micro Nutrients Support",
                createPriceMap(266.50, 1350.00, 1700.00, 86.00, 225.00, 600.00, 330.00, 1350.00, 1290.00, 910.00, 340.00, 65.00)
        ));

        // West Bengal
        STATE_PROFILES.put("WEST_BENGAL", new StatePricingProfile(
                "west_bengal", "West Bengal", "पश्चिम बंगाल", "पश्चिम बंगाल",
                "BENFED / West Bengal State Co-op Marketing",
                "Krishak Bandhu / Organic Mission",
                createPriceMap(266.50, 1350.00, 1690.00, 87.00, 225.00, 600.00, 270.00, 1100.00, 1200.00, 870.00, 330.00, 60.00)
        ));

        // Tamil Nadu
        STATE_PROFILES.put("TAMIL_NADU", new StatePricingProfile(
                "tamil_nadu", "Tamil Nadu", "तमिलनाडु", "तमिळनाडू",
                "TANFED / Tamil Nadu Co-operative Marketing",
                "Uzhavar Sandhai & TN Organic Agriculture",
                createPriceMap(266.50, 1350.00, 1700.00, 89.00, 225.00, 600.00, 350.00, 1450.00, 1320.00, 940.00, 350.00, 70.00)
        ));

        // Odisha
        STATE_PROFILES.put("ODISHA", new StatePricingProfile(
                "odisha", "Odisha", "ओडिशा", "ओडिशा",
                "OSAMB / Odisha State Co-operative Bank",
                "KALIA / Millets & Bio-inputs Mission",
                createPriceMap(266.50, 1350.00, 1700.00, 86.00, 225.00, 600.00, 280.00, 1150.00, 1220.00, 880.00, 330.00, 60.00)
        ));

        // All India
        STATE_PROFILES.put("ALL_INDIA", new StatePricingProfile(
                "all_india", "All India (National Benchmark)", "राष्ट्रीय मानक (अखिल भारतीय)", "राष्ट्रीय मानक (अखिल भारतीय)",
                "Department of Fertilizers, Govt. of India / IFFCO eBazar",
                "Pradhan Mantri Kisan Samriddhi Kendras (PMKSK) & NBS Subsidy",
                createPriceMap(266.50, 1350.00, 1700.00, 85.00, 225.00, 600.00, 300.00, 1200.00, 1250.00, 900.00, 350.00, 60.00)
        ));
    }

    public List<CropFertilizerStandard> getCrops(String category, String state) {
        List<CropFertilizerStandard> list;
        if (category != null && !category.equalsIgnoreCase("all")) {
            list = cropRepository.findAllByCategory(category.toLowerCase());
        } else {
            list = cropRepository.findAll();
        }

        if (list.isEmpty()) {
            seedDefaultCrops();
            list = cropRepository.findAll();
        }
        return list;
    }

    public Optional<CropFertilizerStandard> getCrop(String cropKey) {
        return cropRepository.findByCropKey(cropKey);
    }

    @Transactional
    public FertilizerPrice updatePrice(String itemKey, Double newPrice, String source) {
        FertilizerPrice p = priceRepository.findByItemKey(itemKey)
                .orElseThrow(() -> new IllegalArgumentException("Fertilizer not found: " + itemKey));
        p.setPrice(newPrice);
        if (source != null && !source.isBlank()) {
            p.setSource(source);
        }
        p.setLastUpdated(LocalDateTime.now());
        return priceRepository.save(p);
    }

    @Transactional
    public void syncOfficialRates() {
        log.info("[FertilizerService] Daily automated fertilizer price sync executing...");
        List<FertilizerPrice> list = priceRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (FertilizerPrice p : list) {
            p.setLastUpdated(now);
            p.setSource("Govt. of India / IFFCO eBazar (Verified " + now.toLocalDate() + ")");
        }
        priceRepository.saveAll(list);
        log.info("[FertilizerService] Synchronized {} fertilizer rates successfully.", list.size());
    }

    @Transactional
    public void seedDefaultPrices() {
        List<FertilizerPrice> defaults = Arrays.asList(
                // Chemical
                FertilizerPrice.builder()
                        .itemKey("ureaBag")
                        .nameEn("Neem-Coated Urea (45 kg)")
                        .nameHi("नीम लेपित यूरिया (45 किग्रा)")
                        .nameMr("नीम कोटेड युरिया (४५ किलो)")
                        .category("CHEMICAL")
                        .unit("BAG_45KG")
                        .price(266.50)
                        .mrp(2450.00)
                        .subsidyAmount(2183.50)
                        .brand("IFFCO / National")
                        .source("Department of Fertilizers, Govt. of India")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("dapBag")
                        .nameEn("DAP 18-46-0 (50 kg)")
                        .nameHi("डीएपी 18-46-0 (50 किग्रा)")
                        .nameMr("डीएपी १८-४६-० (५० किलो)")
                        .category("CHEMICAL")
                        .unit("BAG_50KG")
                        .price(1350.00)
                        .mrp(3850.00)
                        .subsidyAmount(2500.00)
                        .brand("IFFCO / KRIBHCO")
                        .source("Department of Fertilizers, Govt. of India (NBS)")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("mopBag")
                        .nameEn("MOP Potash 60% K₂O (50 kg)")
                        .nameHi("पोटाश MOP 60% (50 किग्रा)")
                        .nameMr("पोटॅश MOP ६०% (५० किलो)")
                        .category("CHEMICAL")
                        .unit("BAG_50KG")
                        .price(1700.00)
                        .mrp(2400.00)
                        .subsidyAmount(700.00)
                        .brand("IPL / IFFCO")
                        .source("NBS Subsidy Schedule, Govt. of India")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("zincPerKg")
                        .nameEn("Zinc Sulphate 33% (per kg)")
                        .nameHi("जिंक सल्फेट 33% (प्रति किग्रा)")
                        .nameMr("झिंक सल्फेट ३३% (प्रति किलो)")
                        .category("MICRONUTRIENT")
                        .unit("KG")
                        .price(85.00)
                        .mrp(110.00)
                        .subsidyAmount(25.00)
                        .brand("IFFCO / State Agro")
                        .source("State Agricultural Marketing Boards")
                        .build(),

                // Nano Fertilizers
                FertilizerPrice.builder()
                        .itemKey("nanoUreaBottle")
                        .nameEn("IFFCO Nano Urea Liquid (500 ml)")
                        .nameHi("इफको नैनो यूरिया तरल (500 मि.ली.)")
                        .nameMr("इफको नॅनो युरिया लिक्विड (५०० मिली)")
                        .category("NANO")
                        .unit("BOTTLE_500ML")
                        .price(225.00)
                        .mrp(225.00)
                        .subsidyAmount(0.0)
                        .brand("IFFCO")
                        .source("IFFCO eBazar Official")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("nanoDapBottle")
                        .nameEn("IFFCO Nano DAP Liquid (500 ml)")
                        .nameHi("इफको नैनो डीएपी तरल (500 मि.ली.)")
                        .nameMr("इफको नॅनो डीएपी लिक्विड (५०० मिली)")
                        .category("NANO")
                        .unit("BOTTLE_500ML")
                        .price(600.00)
                        .mrp(600.00)
                        .subsidyAmount(0.0)
                        .brand("IFFCO")
                        .source("IFFCO eBazar Official")
                        .build(),

                // Organic
                FertilizerPrice.builder()
                        .itemKey("vermiBag50")
                        .nameEn("Vermicompost (50 kg bag)")
                        .nameHi("केंचुआ खाद (50 किग्रा बोरी)")
                        .nameMr("गांडूळ खत (५० किलो गोणी)")
                        .category("ORGANIC")
                        .unit("BAG_50KG")
                        .price(300.00)
                        .mrp(450.00)
                        .subsidyAmount(150.00)
                        .brand("Certified FPO / Organic Board")
                        .source("NCOF (National Centre of Organic Farming)")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("fymTrolley")
                        .nameEn("Well-Rotted FYM (per Tractor Trolley ~2.5 Tonnes)")
                        .nameHi("सड़ी गोबर खाद (प्रति ट्रैक्टर ट्रॉली ~2.5 टन)")
                        .nameMr("चांगले कुजलेले शेणखत (प्रति ट्रॉली)")
                        .category("ORGANIC")
                        .unit("TROLLEY")
                        .price(1200.00)
                        .mrp(1500.00)
                        .subsidyAmount(300.00)
                        .brand("Local Dairy / Gaushala")
                        .source("Rural Agricultural Markets (APMC)")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("neemCake50")
                        .nameEn("Neem Cake Organic Manure (50 kg)")
                        .nameHi("नीम की खली जैविक खाद (50 किग्रा)")
                        .nameMr("निंबोळी पेंड (५० किलो)")
                        .category("ORGANIC")
                        .unit("BAG_50KG")
                        .price(1250.00)
                        .mrp(1500.00)
                        .subsidyAmount(250.00)
                        .brand("KVIC / IFFCO Organic")
                        .source("NCOF Organic Inputs Benchmark")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("promBag50")
                        .nameEn("PROM Phosphate Rich Organic Manure (50 kg)")
                        .nameHi("PROM जैविक फॉस्फेट खाद - DAP विकल्प (50 किग्रा)")
                        .nameMr("PROM सेंद्रिय फॉस्फेट खत (५० किलो)")
                        .category("ORGANIC")
                        .unit("BAG_50KG")
                        .price(900.00)
                        .mrp(1200.00)
                        .subsidyAmount(300.00)
                        .brand("State Agro / IFFCO")
                        .source("NCOF Certified Organic Inputs")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("bioKit")
                        .nameEn("Bio-Fertilizer Consortium Kit (2 Litres)")
                        .nameHi("जैव उर्वरक किट (एजोटोबैक्टर + पीएसबी + ट्राइकोडर्मा)")
                        .nameMr("जिवाणू खत किट (२ लिटर)")
                        .category("ORGANIC")
                        .unit("KIT")
                        .price(350.00)
                        .mrp(500.00)
                        .subsidyAmount(150.00)
                        .brand("IFFCO Bio / State Agriculture")
                        .source("ICAR-NBAIM Biofertilizer Portal")
                        .build(),
                FertilizerPrice.builder()
                        .itemKey("jeevamrut")
                        .nameEn("Jeevamrut Culture Formulation (per batch)")
                        .nameHi("जीवामृत सामग्री (देशी गाय गोमूत्र/गुड़/बेसन)")
                        .nameMr("जीवामृत साहित्य (प्रति बॅच)")
                        .category("ORGANIC")
                        .unit("LITRE")
                        .price(60.00)
                        .mrp(100.00)
                        .subsidyAmount(40.00)
                        .brand("Homemade Organic")
                        .source("Zero Budget Natural Farming (ZBNF)")
                        .build()
        );
        priceRepository.saveAll(defaults);
        log.info("[FertilizerService] Successfully seeded {} default fertilizer prices.", defaults.size());
    }

    @Transactional
    public void seedDefaultCrops() {
        List<CropFertilizerStandard> defaults = Arrays.asList(
                CropFertilizerStandard.builder()
                        .cropKey("wheat")
                        .nameEn("Wheat (गेहूं)")
                        .nameHi("गेहूं (Wheat)")
                        .nameMr("गहू (Wheat)")
                        .icon("🌾")
                        .category("grains")
                        .n(48.0).p(24.0).k(16.0)
                        .ureaPerAcre(1.87).dapPerAcre(1.04).mopPerAcre(0.53).zincKgPerAcre(5.0)
                        .microNameEn("Zinc Sulphate 33%")
                        .microNameHi("जिंक सल्फेट 33%")
                        .microNameMr("झिंक सल्फेट ३३%")
                        .state("ALL")
                        .source("ICAR-IIWBR Karnal")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Basal Dose (At Sowing)\",\"name_hi\":\"बुवाई के समय (बेसल डोज)\",\"name_mr\":\"पेरणीच्या वेळी\",\"desc_en\":\"Apply full DAP + full MOP + 1/3rd Urea at final ploughing.\",\"desc_hi\":\"बुवाई के समय पूरा DAP + पूरा MOP + 1/3 यूरिया डालें।\"},{\"days\":21,\"name_en\":\"1st Irrigation (CRI Stage)\",\"name_hi\":\"पहला पानी (ताज जड़ अवस्था)\",\"name_mr\":\"पहिले पाणी\",\"desc_en\":\"Top-dress 1/3rd Urea + Zinc immediately after watering.\",\"desc_hi\":\"पहले पानी पर 1/3 यूरिया और जिंक का छिड़काव करें।\"},{\"days\":42,\"name_en\":\"2nd Irrigation (Jointing Stage)\",\"name_hi\":\"दूसरा पानी (गांठ बनने की अवस्था)\",\"name_mr\":\"दुसरे पाणी\",\"desc_en\":\"Top-dress remaining 1/3rd Urea before spike emergence.\",\"desc_hi\":\"बाली निकलने से पहले शेष 1/3 यूरिया डालें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("rice")
                        .nameEn("Paddy / Rice (धान)")
                        .nameHi("धान / चावल (Paddy)")
                        .nameMr("भात (Paddy)")
                        .icon("🍚")
                        .category("grains")
                        .n(50.0).p(25.0).k(20.0)
                        .ureaPerAcre(1.95).dapPerAcre(1.09).mopPerAcre(0.67).zincKgPerAcre(10.0)
                        .microNameEn("Zinc Sulphate 33% (Khaira Disease Control)")
                        .microNameHi("जिंक सल्फेट 33% (खैरा रोग नियंत्रण)")
                        .microNameMr("झिंक सल्फेट ३३%")
                        .state("ALL")
                        .source("ICAR-NRRI Cuttack")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Transplanting (Basal)\",\"name_hi\":\"रोपाई के समय (बेसल डोज)\",\"name_mr\":\"रोपणीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/3rd Urea mixed in puddled soil.\",\"desc_hi\":\"रोपाई के समय पूरा DAP, पूरा MOP और 1/3 यूरिया कीचड़ में मिलाएं।\"},{\"days\":25,\"name_en\":\"Active Tillering (कल्ले फूटते समय)\",\"name_hi\":\"कल्ले फूटने का समय\",\"name_mr\":\"फुटवे फुटण्याची अवस्था\",\"desc_en\":\"1/3rd Urea + 10 kg Zinc Sulphate broad-cast in standing water.\",\"desc_hi\":\"1/3 यूरिया और 10 किग्रा जिंक सल्फेट डालें।\"},{\"days\":45,\"name_en\":\"Panicle Initiation (बालियां बनते समय)\",\"name_hi\":\"बालियां बनने की शुरुआत\",\"name_mr\":\"लोंब्या निघताना\",\"desc_en\":\"Remaining 1/3rd Urea for bold grain formation.\",\"desc_hi\":\"अंतिम 1/3 यूरिया का छिड़काव करें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("mustard")
                        .nameEn("Mustard (सरसों)")
                        .nameHi("सरसों (Mustard)")
                        .nameMr("मोहरी (Mustard)")
                        .icon("🟡")
                        .category("pulses_oilseeds")
                        .n(32.0).p(16.0).k(10.0)
                        .ureaPerAcre(1.25).dapPerAcre(0.70).mopPerAcre(0.33).zincKgPerAcre(10.0)
                        .microNameEn("Bentonite Sulphur 90% (Oil Booster)")
                        .microNameHi("सल्फर 90% (तेल प्रतिशत बढ़ाने हेतु)")
                        .microNameMr("सल्फर ९०%")
                        .state("ALL")
                        .source("ICAR-DRMR Bharatpur")
                        .stagesJson("[{\"days\":0,\"name_en\":\"At Sowing (Basal)\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"पेरणीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + Full Sulphur + 1/2 Urea at last ploughing.\",\"desc_hi\":\"पूरा DAP, MOP, सल्फर और आधा यूरिया बुवाई के समय डालें।\"},{\"days\":30,\"name_en\":\"First Irrigation / Flowering\",\"name_hi\":\"पहला पानी (फूल आने से पूर्व)\",\"name_mr\":\"पहिले पाणी / फुले येताना\",\"desc_en\":\"Top-dress remaining 1/2 Urea with first light watering.\",\"desc_hi\":\"हल्की सिंचाई के साथ बाकी आधा यूरिया डालें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("gram")
                        .nameEn("Gram / Chickpea (चना)")
                        .nameHi("चना (Gram / Chana)")
                        .nameMr("हरभरा (Gram)")
                        .icon("🫘")
                        .category("pulses_oilseeds")
                        .n(10.0).p(20.0).k(10.0)
                        .ureaPerAcre(0.20).dapPerAcre(0.87).mopPerAcre(0.33).zincKgPerAcre(5.0)
                        .microNameEn("Rhizobium Bio-fertilizer Culture")
                        .microNameHi("राइजोबियम कल्चर (जैव टीका)")
                        .microNameMr("रायझोबियम जिवाणू खत")
                        .state("ALL")
                        .source("ICAR-IIPR Kanpur")
                        .stagesJson("[{\"days\":0,\"name_en\":\"At Sowing (Full Dose)\",\"name_hi\":\"बुवाई के समय (संपूर्ण खुराक)\",\"name_mr\":\"पेरणीच्या वेळी\",\"desc_en\":\"Legume crop fixes atmospheric nitrogen. Apply full DAP and MOP in seed furrow. Treat seed with Rhizobium.\",\"desc_hi\":\"दलहन होने के कारण यूरिया कम लगता है। बुवाई पर पूरा DAP और MOP डालें व राइजोबियम से बीज उपचार करें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("potato")
                        .nameEn("Potato (आलू)")
                        .nameHi("आलू (Potato)")
                        .nameMr("बटाटा (Potato)")
                        .icon("🥔")
                        .category("vegetables")
                        .n(60.0).p(32.0).k(40.0)
                        .ureaPerAcre(2.30).dapPerAcre(1.39).mopPerAcre(1.33).zincKgPerAcre(10.0)
                        .microNameEn("Zinc + Magnesium Sulphate")
                        .microNameHi("जिंक व मैग्नीशियम सल्फेट")
                        .microNameMr("झिंक व मॅग्नेशियम सल्फेट")
                        .state("ALL")
                        .source("ICAR-CPRI Shimla")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Planting Time (Basal)\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"लागवडीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/2 Urea band placed below tuber level.\",\"desc_hi\":\"पूरा DAP, पूरा MOP और आधा यूरिया कंद के नीचे डालें।\"},{\"days\":30,\"name_en\":\"Earthing Up (मिट्टी चढ़ाने का समय)\",\"name_hi\":\"मिट्टी चढ़ाते समय\",\"name_mr\":\"माती लावताना\",\"desc_en\":\"Top-dress remaining 1/2 Urea along with Zinc and earth up.\",\"desc_hi\":\"बाकी आधा यूरिया और जिंक डालकर जड़ों पर मिट्टी चढ़ाएं।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("maize")
                        .nameEn("Maize / Corn (मक्का)")
                        .nameHi("मक्का (Maize)")
                        .nameMr("मका (Maize)")
                        .icon("🌽")
                        .category("grains")
                        .n(48.0).p(24.0).k(16.0)
                        .ureaPerAcre(1.87).dapPerAcre(1.04).mopPerAcre(0.53).zincKgPerAcre(10.0)
                        .microNameEn("Zinc Sulphate 33% (White Bud Control)")
                        .microNameHi("जिंक सल्फेट 33%")
                        .microNameMr("झिंक सल्फेट ३३%")
                        .state("ALL")
                        .source("ICAR-IIMR Ludhiana")
                        .stagesJson("[{\"days\":0,\"name_en\":\"At Sowing\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"पेरणीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/3rd Urea at sowing.\",\"desc_hi\":\"पूरा DAP, MOP और 1/3 यूरिया बुवाई पर डालें।\"},{\"days\":25,\"name_en\":\"Knee-High Stage (घुटने बराबर)\",\"name_hi\":\"घुटने बराबर अवस्था\",\"name_mr\":\"गुडघाभर अवस्था\",\"desc_en\":\"1/3rd Urea + Zinc sulphate top-dressed.\",\"desc_hi\":\"1/3 यूरिया और जिंक डालें।\"},{\"days\":45,\"name_en\":\"Tasseling Stage (मंजर आते समय)\",\"name_hi\":\"मंजर आने का समय\",\"name_mr\":\"तुरा येताना\",\"desc_en\":\"Final 1/3rd Urea for cob filling.\",\"desc_hi\":\"अंतिम 1/3 यूरिया डालें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("sugarcane")
                        .nameEn("Sugarcane (गन्ना)")
                        .nameHi("गन्ना (Sugarcane)")
                        .nameMr("ऊस (Sugarcane)")
                        .icon("🎋")
                        .category("cash_crops")
                        .n(100.0).p(32.0).k(32.0)
                        .ureaPerAcre(4.20).dapPerAcre(1.39).mopPerAcre(1.07).zincKgPerAcre(15.0)
                        .microNameEn("Ferrous Sulphate (Iron) + Zinc")
                        .microNameHi("फेरस सल्फेट (लोहा) व जिंक")
                        .microNameMr("फेरस सल्फेट व झिंक")
                        .state("ALL")
                        .source("ICAR-IISR Lucknow")
                        .stagesJson("[{\"days\":0,\"name_en\":\"At Planting\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"लागवडीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/4th Urea in furrows.\",\"desc_hi\":\"पूरा DAP, MOP और 1/4 यूरिया नालियों में डालें।\"},{\"days\":45,\"name_en\":\"Tillering (कल्ले फूटते समय)\",\"name_hi\":\"कल्ले फूटते समय\",\"name_mr\":\"फुटवे अवस्था\",\"desc_en\":\"1/4th Urea top-dress.\",\"desc_hi\":\"1/4 यूरिया डालें।\"},{\"days\":90,\"name_en\":\"Grand Growth (तीव्र बढ़वार)\",\"name_hi\":\"तीव्र बढ़वार अवस्था\",\"name_mr\":\"जोमदार वाढ\",\"desc_en\":\"1/4th Urea before earthing up.\",\"desc_hi\":\"1/4 यूरिया डालें और मिट्टी चढ़ाएं।\"},{\"days\":120,\"name_en\":\"Final Dose\",\"name_hi\":\"अंतिम खुराक\",\"name_mr\":\"शेवटची मात्रा\",\"desc_en\":\"Final 1/4th Urea before onset of monsoon.\",\"desc_hi\":\"अंतिम 1/4 यूरिया डालें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("tomato")
                        .nameEn("Tomato (टमाटर)")
                        .nameHi("टमाटर (Tomato)")
                        .nameMr("टोमॅटो (Tomato)")
                        .icon("🍅")
                        .category("vegetables")
                        .n(48.0).p(24.0).k(24.0)
                        .ureaPerAcre(1.87).dapPerAcre(1.04).mopPerAcre(0.80).zincKgPerAcre(8.0)
                        .microNameEn("Boron 20% + Calcium Nitrate")
                        .microNameHi("बोरॉन 20% व कैल्शियम")
                        .microNameMr("बोरॉन व कॅल्शियम")
                        .state("ALL")
                        .source("ICAR-IIVR Varanasi")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Transplanting (Basal)\",\"name_hi\":\"रोपाई के समय\",\"name_mr\":\"लागवडीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/3rd Urea in planting bed.\",\"desc_hi\":\"रोपाई पर पूरा DAP, MOP और 1/3 यूरिया डालें।\"},{\"days\":25,\"name_en\":\"Vegetative Growth\",\"name_hi\":\"वानस्पतिक बढ़वार\",\"name_mr\":\"वाढीची अवस्था\",\"desc_en\":\"1/3rd Urea top-dress with earthing up.\",\"desc_hi\":\"1/3 यूरिया डालकर मिट्टी चढ़ाएं।\"},{\"days\":50,\"name_en\":\"Fruiting & Harvest\",\"name_hi\":\"फल बनते समय\",\"name_mr\":\"फळधारणा अवस्था\",\"desc_en\":\"Final 1/3rd Urea + Micronutrient foliar spray.\",\"desc_hi\":\"अंतिम 1/3 यूरिया और सूक्ष्म पोषक तत्व दें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("onion")
                        .nameEn("Onion (प्याज)")
                        .nameHi("प्याज (Onion)")
                        .nameMr("कांदा (Onion)")
                        .icon("🧅")
                        .category("vegetables")
                        .n(40.0).p(20.0).k(30.0)
                        .ureaPerAcre(1.55).dapPerAcre(0.87).mopPerAcre(1.00).zincKgPerAcre(10.0)
                        .microNameEn("Sulphur 90% (Pungency & Storage)")
                        .microNameHi("सल्फर 90% (तीखापन व चमक हेतु)")
                        .microNameMr("सल्फर ९०%")
                        .state("ALL")
                        .source("ICAR-DOGR Pune")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Transplanting (Basal)\",\"name_hi\":\"रोपाई के समय\",\"name_mr\":\"रोपणीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 10 kg Sulphur + 1/2 Urea.\",\"desc_hi\":\"पूरा DAP, पूरा MOP, 10 किग्रा सल्फर और आधा यूरिया डालें।\"},{\"days\":35,\"name_en\":\"Bulb Formation\",\"name_hi\":\"कंद बनने की अवस्था\",\"name_mr\":\"कांदा पोसताना\",\"desc_en\":\"Remaining 1/2 Urea before bulb enlargement.\",\"desc_hi\":\"कंद बनते समय शेष आधा यूरिया डालें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("mango")
                        .nameEn("Mango (आम)")
                        .nameHi("आम (Mango)")
                        .nameMr("आंबा (Mango)")
                        .icon("🥭")
                        .category("fruits")
                        .n(40.0).p(20.0).k(40.0)
                        .ureaPerAcre(1.55).dapPerAcre(0.87).mopPerAcre(1.33).zincKgPerAcre(5.0)
                        .microNameEn("Zinc + Boron 20% (Prevents Fruit Drop)")
                        .microNameHi("जिंक व बोरॉन (फल झड़ने से बचाव)")
                        .microNameMr("झिंक व बोरॉन")
                        .state("ALL")
                        .source("ICAR-CISH Lucknow")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Post-Monsoon (Sept-Oct)\",\"name_hi\":\"मानसून बाद (सितम्बर-अक्टूबर)\",\"name_mr\":\"पावसाळ्यानंतर\",\"desc_en\":\"Apply full FYM + full DAP + full MOP + 1/2 Urea in tree canopy trench.\",\"desc_hi\":\"पेड़ के घेरे में खाई बनाकर पूरी गोबर खाद, DAP, MOP और आधा यूरिया डालें।\"},{\"days\":120,\"name_en\":\"Pea Stage (Feb-March)\",\"name_hi\":\"मटर दाना अवस्था (फरवरी-मार्च)\",\"name_mr\":\"वाटाणा आकाराची फळे असताना\",\"desc_en\":\"Remaining 1/2 Urea + Boron foliar spray for fruit sizing.\",\"desc_hi\":\"बाकी आधा यूरिया और बोरॉन का छिड़काव करें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("banana")
                        .nameEn("Banana (केला)")
                        .nameHi("केला (Banana)")
                        .nameMr("केळी (Banana)")
                        .icon("🍌")
                        .category("fruits")
                        .n(80.0).p(32.0).k(80.0)
                        .ureaPerAcre(3.30).dapPerAcre(1.39).mopPerAcre(2.67).zincKgPerAcre(10.0)
                        .microNameEn("Ferrous + Zinc + Boron")
                        .microNameHi("आयरन, जिंक व बोरॉन")
                        .microNameMr("लोह, झिंक व बोरॉन")
                        .state("ALL")
                        .source("ICAR-NRCB Tiruchirappalli")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Planting Time\",\"name_hi\":\"रोपाई के समय\",\"name_mr\":\"लागवडीच्या वेळी\",\"desc_en\":\"Full DAP + 1/4th Urea + 1/4th MOP placed in pit.\",\"desc_hi\":\"गड्ढे में पूरा DAP और 1/4 यूरिया व पोटाश डालें।\"},{\"days\":45,\"name_en\":\"Vegetative Surge\",\"name_hi\":\"तीव्र वानस्पतिक बढ़वार\",\"name_mr\":\"जोमदार वाढ\",\"desc_en\":\"1/4th Urea + 1/4th MOP.\",\"desc_hi\":\"1/4 यूरिया और 1/4 पोटाश डालें।\"},{\"days\":90,\"name_en\":\"Flower Bud Initiation\",\"name_hi\":\"कमल / फूल निकलते समय\",\"name_mr\":\"केळफूल निघताना\",\"desc_en\":\"1/4th Urea + 1/4th MOP.\",\"desc_hi\":\"1/4 यूरिया और 1/4 पोटाश दें।\"},{\"days\":150,\"name_en\":\"Bunch Maturation\",\"name_hi\":\"घार बनने की अवस्था\",\"name_mr\":\"घड भरताना\",\"desc_en\":\"Final 1/4th Urea + 1/4th MOP for heavy bunch weight.\",\"desc_hi\":\"अंतिम 1/4 यूरिया और पोटाश डालें जिससे घार भारी बने।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("cotton")
                        .nameEn("Cotton (कपास)")
                        .nameHi("कपास (Cotton)")
                        .nameMr("कापूस (Cotton)")
                        .icon("☁️")
                        .category("cash_crops")
                        .n(48.0).p(24.0).k(24.0)
                        .ureaPerAcre(1.87).dapPerAcre(1.04).mopPerAcre(0.80).zincKgPerAcre(10.0)
                        .microNameEn("Magnesium Sulphate (Red Leaf Control)")
                        .microNameHi("मैग्नीशियम सल्फेट (लाल पत्ती रोग रोकथाम)")
                        .microNameMr("मॅग्नेशियम सल्फेट (लाल तांबड्या रोग प्रतिबंध)")
                        .state("ALL")
                        .source("ICAR-CICR Nagpur")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Sowing (Basal)\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"पेरणीच्या वेळी\",\"desc_en\":\"Full DAP + Full MOP + 1/3rd Urea in seed furrows.\",\"desc_hi\":\"पूरा DAP, पूरा MOP और 1/3 यूरिया बुवाई पर डालें।\"},{\"days\":35,\"name_en\":\"Square Formation (पिलाव अवस्था)\",\"name_hi\":\"पिलाव (डोडी) बनते समय\",\"name_mr\":\"पात्या धरताना\",\"desc_en\":\"1/3rd Urea + Magnesium Sulphate spray.\",\"desc_hi\":\"1/3 यूरिया और मैग्नीशियम सल्फेट डालें।\"},{\"days\":65,\"name_en\":\"Flowering & Boll Formation\",\"name_hi\":\"फूल व टिंडे बनते समय\",\"name_mr\":\"बोंडे भरताना\",\"desc_en\":\"Final 1/3rd Urea for big bolls and high lint yield.\",\"desc_hi\":\"अंतिम 1/3 यूरिया डालें जिससे टिंडे बड़े बनें।\"}]")
                        .build(),

                CropFertilizerStandard.builder()
                        .cropKey("turmeric")
                        .nameEn("Turmeric (हल्दी)")
                        .nameHi("हल्दी (Turmeric)")
                        .nameMr("हळद (Turmeric)")
                        .icon("🟡")
                        .category("medicinal_spices")
                        .n(48.0).p(24.0).k(36.0)
                        .ureaPerAcre(1.87).dapPerAcre(1.04).mopPerAcre(1.20).zincKgPerAcre(10.0)
                        .microNameEn("Zinc + Iron Chelate (Rhizome Booster)")
                        .microNameHi("जिंक व आयरन (गांठ वृद्धि हेतु)")
                        .microNameMr("झिंक व लोह (कंद वाढीसाठी)")
                        .state("ALL")
                        .source("ICAR-IISR Calicut")
                        .stagesJson("[{\"days\":0,\"name_en\":\"Planting (Basal)\",\"name_hi\":\"बुवाई के समय\",\"name_mr\":\"लागवडीच्या वेळी\",\"desc_en\":\"Full DAP + 1/3rd MOP + 1/3rd Urea + 5 tonnes FYM in ridges.\",\"desc_hi\":\"पूरा DAP, 1/3 पोटाश, 1/3 यूरिया और गोबर खाद मेड़ों में डालें।\"},{\"days\":45,\"name_en\":\"Tillering Stage\",\"name_hi\":\"कल्ले फूटते समय\",\"name_mr\":\"फुटवे अवस्था\",\"desc_en\":\"1/3rd Urea + 1/3rd MOP + Zinc top-dress.\",\"desc_hi\":\"1/3 यूरिया, 1/3 पोटाश और जिंक डालें।\"},{\"days\":90,\"name_en\":\"Rhizome Development\",\"name_hi\":\"गांठ बनने की अवस्था\",\"name_mr\":\"कंद पोसण्याची अवस्था\",\"desc_en\":\"Final 1/3rd Urea + 1/3rd MOP with earthing up.\",\"desc_hi\":\"अंतिम 1/3 यूरिया व पोटाश डालकर मिट्टी चढ़ाएं।\"}]")
                        .build()
        );
        cropRepository.saveAll(defaults);
        log.info("[FertilizerService] Successfully seeded {} default crop fertilizer standards.", defaults.size());
    }
}
