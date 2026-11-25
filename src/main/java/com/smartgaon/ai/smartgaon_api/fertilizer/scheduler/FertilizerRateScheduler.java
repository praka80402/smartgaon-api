package com.smartgaon.ai.smartgaon_api.fertilizer.scheduler;

import com.smartgaon.ai.smartgaon_api.fertilizer.service.FertilizerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "smartgaon.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class FertilizerRateScheduler {

    private final FertilizerService fertilizerService;

    /**
     * Daily background cron job at 4:00 AM to sync / verify fertilizer prices
     * against official Department of Fertilizers & IFFCO eBazar price notifications.
     */
    @Scheduled(cron = "0 0 4 * * ?")
    public void runDailyPriceSync() {
        log.info("[FertilizerRateScheduler] Running daily automated fertilizer price sync...");
        try {
            fertilizerService.syncOfficialRates();
            log.info("[FertilizerRateScheduler] Fertilizer rates verified successfully.");
        } catch (Exception e) {
            log.error("[FertilizerRateScheduler] Fertilizer sync failed: {}", e.getMessage(), e);
        }
    }
}
