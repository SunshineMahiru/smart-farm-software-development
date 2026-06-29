package com.smartfarm.modules.plan.job;

import com.smartfarm.modules.plan.service.PlantingPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlanLifecycleScheduler {

    private final PlantingPlanService plantingPlanService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void refreshLifecycle() {
        log.info("Start scheduled planting plan lifecycle refresh");
        plantingPlanService.refreshLifecycleAndBroadcast();
        log.info("Finish scheduled planting plan lifecycle refresh");
    }
}
