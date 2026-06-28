package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "IoT概览统计")
public class IotOverviewVO {
    @Schema(description = "传感器总数", example = "12")
    private Long totalSensors;
    @Schema(description = "在线传感器数", example = "9")
    private Long onlineSensors;
    @Schema(description = "离线传感器数", example = "3")
    private Long offlineSensors;
    @Schema(description = "今日采集数据条数", example = "356")
    private Long todayDataCount;
    @Schema(description = "告警总数", example = "18")
    private Long totalAlerts;
    @Schema(description = "未处理告警数", example = "5")
    private Long pendingAlerts;
    @Schema(description = "最近采集时间", example = "2026-06-28T09:30:00")
    private LocalDateTime latestCollectTime;
}
