package com.smartfarm.modules.iot.job;

import com.smartfarm.modules.iot.service.IotReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IotDailyReportScheduler {

    private final IotReportService iotReportService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void generateMorningReport() {
        log.info("开始执行 IoT 农情日报定时任务");
        iotReportService.generateDailyReport();
    }
}
