package com.smartgaon.ai.smartgaon_api.gaontalent.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

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
        winner.setCompetitionId(entry.getCompetitionId());
        winnerRepo.save(winner);

        return "Winner declared!";
    }

    public List<TalentEntry> getAllWinners() {
        return entryRepo.findAll()
                .stream()
                .filter(TalentEntry::isWinner)
                .toList();
    }

    public List<TalentEntry> getWinnersByFilter(Long competitionId, Integer year, Integer month) {
        if (competitionId == null && year == null && month == null) {
            throw new RuntimeException("At least one filter parameter is required.");
        }

        Integer resolvedYear = year;
        if (resolvedYear == null && (competitionId != null || month != null)) {
            resolvedYear = Year.now().getValue();
        }

        List<Long> winnerIds = winnerRepo.findByCriteria(competitionId, resolvedYear, month)
                .stream()
                .map(TalentWinner::getEntryId)
                .toList();

        List<TalentEntry> winners = entryRepo.findAllById(winnerIds);
        return winners.stream()
                .sorted((a, b) -> Long.compare(
                        winnerIds.indexOf(a.getId()),
                        winnerIds.indexOf(b.getId())))
                .toList();
    }
}
