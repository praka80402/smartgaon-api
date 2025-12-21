package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentEntry;
import com.smartgaon.ai.smartgaon_api.gaontalent.Entity.TalentWinner;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentEntryRepository;
import com.smartgaon.ai.smartgaon_api.gaontalent.Repository.TalentWinnerRepository;

@Service
@RequiredArgsConstructor
public class WinnerService {

    private final TalentWinnerRepository winnerRepo;
    private final TalentEntryRepository entryRepo;

    public String declareWinner(Long entryId) {

        TalentEntry entry = entryRepo.findById(entryId)
                .orElseThrow(() -> new RuntimeException("Entry not found"));

        entry.setWinner(true);
        entryRepo.save(entry);

        TalentWinner winner = new TalentWinner();
        winner.setEntryId(entryId);
        winnerRepo.save(winner);

        return "Winner declared!";
    }

    public Object getWinners() {
        return entryRepo.findAll()
                .stream()
                .filter(TalentEntry::isWinner);
    }
}

