package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IotOverviewVO {
    private Long totalSensors;
    private Long onlineSensors;
    private Long offlineSensors;
    private Long todayDataCount;
    private Long totalAlerts;
    private Long pendingAlerts;
    private LocalDateTime latestCollectTime;
}
