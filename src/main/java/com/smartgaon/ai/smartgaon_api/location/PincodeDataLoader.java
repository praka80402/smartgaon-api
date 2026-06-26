package com.smartgaon.ai.smartgaon_api.location;

import com.smartgaon.ai.smartgaon_api.location.entity.District;
import com.smartgaon.ai.smartgaon_api.location.entity.Pincode;
import com.smartgaon.ai.smartgaon_api.location.entity.State;
import com.smartgaon.ai.smartgaon_api.location.repository.DistrictRepository;
import com.smartgaon.ai.smartgaon_api.location.repository.PincodeRepository;
import com.smartgaon.ai.smartgaon_api.location.repository.StateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class PincodeDataLoader implements CommandLineRunner {

    private final StateRepository stateRepo;
    private final DistrictRepository districtRepo;
    private final PincodeRepository pincodeRepo;

    public PincodeDataLoader(StateRepository stateRepo,
                             DistrictRepository districtRepo,
                             PincodeRepository pincodeRepo) {
        this.stateRepo = stateRepo;
        this.districtRepo = districtRepo;
        this.pincodeRepo = pincodeRepo;
    }

    @Override
    public void run(String... args) throws Exception {

        // Safety: agar pincodes pehle se hain, dobara load mat karo
        if (pincodeRepo.count() > 0) {
            System.out.println(">> Pincodes already loaded. Skipping import.");
            return;
        }

        System.out.println(">> Pincode import started...");

        // 1. States ko ek map me le lo (naam -> id), case-insensitive
        Map<String, Long> stateMap = new HashMap<>();
        for (State s : stateRepo.findAll()) {
            stateMap.put(s.getName().trim().toLowerCase(), s.getId());
        }

        // 2. Districts ko yaad rakhne ke liye (stateId|districtName -> districtId)
        Map<String, Long> districtMap = new HashMap<>();

        List<Pincode> pincodeBatch = new ArrayList<>();
        int total = 0, skipped = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("pincodes.csv").getInputStream(), StandardCharsets.UTF_8))) {

            String line = br.readLine(); // header skip
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = parseCsv(line);
                // columns: 0 circle,1 region,2 division,3 office,4 pincode,5 type,6 delivery,7 district,8 state,...
                if (cols.length < 9) { skipped++; continue; }

                String pincode = clean(cols[4]);
                String districtName = clean(cols[7]);
                String stateName = clean(cols[8]);

                if (pincode.isEmpty() || districtName.isEmpty() || stateName.isEmpty()) {
                    skipped++;
                    continue;
                }

                // State match
                Long stateId = stateMap.get(stateName.toLowerCase());
                if (stateId == null) { skipped++; continue; } // state DB me nahi mili

                // District nikalo ya banao
                String dKey = stateId + "|" + districtName.toLowerCase();
                Long districtId = districtMap.get(dKey);
                if (districtId == null) {
                    District d = District.builder()
                            .name(toTitle(districtName))
                            .stateId(stateId)
                            .build();
                    d = districtRepo.save(d);
                    districtId = d.getId();
                    districtMap.put(dKey, districtId);
                }

                // Pincode batch me jodo
                pincodeBatch.add(Pincode.builder()
                        .pincode(pincode)
                        .areaName(toTitle(clean(cols[3]))) // office name ko area maan rahe
                        .districtId(districtId)
                        .build());

                total++;

                // Har 2000 par ek baar save (memory bachane ke liye)
                if (pincodeBatch.size() >= 2000) {
                    pincodeRepo.saveAll(pincodeBatch);
                    pincodeBatch.clear();
                }
            }
        }

        // Bache hue save karo
        if (!pincodeBatch.isEmpty()) {
            pincodeRepo.saveAll(pincodeBatch);
        }

        System.out.println(">> Import done. Pincodes added: " + total
                + ", Districts: " + districtMap.size()
                + ", Skipped rows: " + skipped);
    }

    // Quotes hata kar, trim karke saaf string
    private String clean(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.trim();
    }

    // Simple CSV parser (quotes ke andar comma handle karta hai)
    private String[] parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    // BIHAR -> Bihar, PATNA -> Patna
    private String toTitle(String s) {
        if (s == null || s.isEmpty()) return s;
        String[] words = s.toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.isEmpty()) continue;
            sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }
}