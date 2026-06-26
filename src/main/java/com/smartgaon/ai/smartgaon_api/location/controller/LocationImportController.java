package com.smartgaon.ai.smartgaon_api.location.controller;

import com.smartgaon.ai.smartgaon_api.location.entity.District;
import com.smartgaon.ai.smartgaon_api.location.entity.Pincode;
import com.smartgaon.ai.smartgaon_api.location.entity.State;
import com.smartgaon.ai.smartgaon_api.location.repository.DistrictRepository;
import com.smartgaon.ai.smartgaon_api.location.repository.PincodeRepository;
import com.smartgaon.ai.smartgaon_api.location.repository.StateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/location")
public class LocationImportController {

    private final StateRepository stateRepo;
    private final DistrictRepository districtRepo;
    private final PincodeRepository pincodeRepo;

    public LocationImportController(StateRepository stateRepo,
                                    DistrictRepository districtRepo,
                                    PincodeRepository pincodeRepo) {
        this.stateRepo = stateRepo;
        this.districtRepo = districtRepo;
        this.pincodeRepo = pincodeRepo;
    }

    private static final Map<String, String> STATE_CODES = new HashMap<>();
    static {
        STATE_CODES.put("andhra pradesh", "AP");
        STATE_CODES.put("arunachal pradesh", "AR");
        STATE_CODES.put("assam", "AS");
        STATE_CODES.put("bihar", "BR");
        STATE_CODES.put("chhattisgarh", "CG");
        STATE_CODES.put("goa", "GA");
        STATE_CODES.put("gujarat", "GJ");
        STATE_CODES.put("haryana", "HR");
        STATE_CODES.put("himachal pradesh", "HP");
        STATE_CODES.put("jharkhand", "JH");
        STATE_CODES.put("karnataka", "KA");
        STATE_CODES.put("kerala", "KL");
        STATE_CODES.put("madhya pradesh", "MP");
        STATE_CODES.put("maharashtra", "MH");
        STATE_CODES.put("manipur", "MN");
        STATE_CODES.put("meghalaya", "ML");
        STATE_CODES.put("mizoram", "MZ");
        STATE_CODES.put("nagaland", "NL");
        STATE_CODES.put("odisha", "OD");
        STATE_CODES.put("punjab", "PB");
        STATE_CODES.put("rajasthan", "RJ");
        STATE_CODES.put("sikkim", "SK");
        STATE_CODES.put("tamil nadu", "TN");
        STATE_CODES.put("telangana", "TS");
        STATE_CODES.put("tripura", "TR");
        STATE_CODES.put("uttar pradesh", "UP");
        STATE_CODES.put("uttarakhand", "UK");
        STATE_CODES.put("west bengal", "WB");
        STATE_CODES.put("andaman and nicobar islands", "AN");
        STATE_CODES.put("andaman & nicobar islands", "AN");
        STATE_CODES.put("chandigarh", "CH");
        STATE_CODES.put("dadra and nagar haveli and daman and diu", "DH");
        STATE_CODES.put("dadra & nagar haveli", "DH");
        STATE_CODES.put("daman and diu", "DH");
        STATE_CODES.put("delhi", "DL");
        STATE_CODES.put("jammu and kashmir", "JK");
        STATE_CODES.put("jammu & kashmir", "JK");
        STATE_CODES.put("ladakh", "LA");
        STATE_CODES.put("lakshadweep", "LD");
        STATE_CODES.put("puducherry", "PY");
        STATE_CODES.put("pondicherry", "PY");
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        Map<String, Long> result = new LinkedHashMap<>();
        result.put("states", stateRepo.count());
        result.put("districts", districtRepo.count());
        result.put("pincodes", pincodeRepo.count());
        return result;
    }

    @PostMapping("/import")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSV file empty hai ya nahi mili"));
        }

        if (pincodeRepo.count() > 0) {
            return ResponseEntity.ok(Map.of(
                    "status", "skipped",
                    "message", "Data pehle se loaded hai. Pehle clear karein tabhi dobara import hoga.",
                    "states", stateRepo.count(),
                    "districts", districtRepo.count(),
                    "pincodes", pincodeRepo.count()
            ));
        }

        Map<String, Long> stateMap = new HashMap<>();
        Map<String, Long> districtMap = new HashMap<>();
        List<Pincode> batch = new ArrayList<>();
        int total = 0, skipped = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = parseCsv(line);
                if (cols.length < 9) { skipped++; continue; }

                String pincode = clean(cols[4]);
                String districtName = clean(cols[7]);
                String stateName = normalizeState(clean(cols[8]));

                // Junk/khaali state ya district skip
                if (pincode.isEmpty() || districtName.isEmpty() || stateName.isEmpty()
                        || isJunk(stateName) || isJunk(districtName)) {
                    skipped++; continue;
                }

                String sKey = stateName.toLowerCase();
                Long stateId = stateMap.get(sKey);
                if (stateId == null) {
                    String code = STATE_CODES.get(sKey);
                    State st = stateRepo.save(State.builder()
                            .name(toTitle(stateName))
                            .stateCode(code)
                            .build());
                    stateId = st.getId();
                    stateMap.put(sKey, stateId);
                }

                String dKey = stateId + "|" + districtName.toLowerCase();
                Long districtId = districtMap.get(dKey);
                if (districtId == null) {
                    District d = districtRepo.save(District.builder()
                            .name(toTitle(districtName))
                            .stateId(stateId)
                            .build());
                    districtId = d.getId();
                    districtMap.put(dKey, districtId);
                }

                batch.add(Pincode.builder()
                        .pincode(pincode)
                        .areaName(toTitle(clean(cols[3])))
                        .districtId(districtId)
                        .build());
                total++;

                if (batch.size() >= 2000) {
                    pincodeRepo.saveAll(batch);
                    batch.clear();
                }
            }
            if (!batch.isEmpty()) pincodeRepo.saveAll(batch);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Import fail hua: " + e.getMessage()));
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "success");
        result.put("statesAdded", stateMap.size());
        result.put("districtsAdded", districtMap.size());
        result.put("pincodesAdded", total);
        result.put("skippedRows", skipped);
        return ResponseEntity.ok(result);
    }

    // "The Dadra..." -> "Dadra..."
    private String normalizeState(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.toLowerCase().startsWith("the ")) {
            s = s.substring(4).trim();
        }
        return s;
    }

    // Khaali/bekaar values (NA, NULL, -, .)
    private boolean isJunk(String s) {
        if (s == null) return true;
        String t = s.trim().toLowerCase();
        return t.isEmpty() || t.equals("na") || t.equals("n/a")
                || t.equals("null") || t.equals("-") || t.equals(".");
    }

    private String clean(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.trim();
    }

    private String[] parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') inQuotes = !inQuotes;
            else if (c == ',' && !inQuotes) { out.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    private String toTitle(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] w = s.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String x : w) {
            if (x.isEmpty()) continue;
            sb.append(Character.toUpperCase(x.charAt(0))).append(x.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}