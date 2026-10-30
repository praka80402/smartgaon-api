package com.smartgaon.ai.smartgaon_api.mandi.scheduler;

import com.smartgaon.ai.smartgaon_api.mandi.service.MandiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MandiScheduler {

    private final MandiService mandiService;

    /**
     * Daily Cron Job: Sync fresh Mandi rates from data.gov.in Agmarknet API every evening at 7:30 PM IST.
     * Most APMC mandis conclude trading by 6:00 PM and post final modal rates by 7:00 PM.
     */
    @Scheduled(cron = "0 30 19 * * ?", zone = "Asia/Kolkata")
    public void runDailyMandiSync() {
        log.info("[MandiScheduler] Triggering scheduled evening Mandi rates sync (19:30 IST)...");
        try {
            Map<String, Object> syncResult = mandiService.syncGovtMandiData(null, 100);
            log.info("[MandiScheduler] Evening Mandi sync completed successfully: {}", syncResult);
        } catch (Exception e) {
            log.error("[MandiScheduler] Error running scheduled Mandi sync", e);
        }
    }

    /**
     * Daily Retention Purge: Runs every night at 2:00 AM IST.
     * Deletes Mandi records where arrivalDate < (Today - 30 Days) to keep database optimal and prevent storage bloat.
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Kolkata")
    public void runDailyRetentionPurge() {
        log.info("[MandiScheduler] Triggering scheduled 30-day retention cleanup (02:00 IST)...");
        try {
            Map<String, Object> purgeResult = mandiService.purgeOldMandiRates();
            log.info("[MandiScheduler] Daily retention purge completed successfully: {}", purgeResult);
        } catch (Exception e) {
            log.error("[MandiScheduler] Error running scheduled 30-day retention purge", e);
        }
    }
}
