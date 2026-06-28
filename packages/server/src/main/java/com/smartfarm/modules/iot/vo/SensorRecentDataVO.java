package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "传感器最近采集数据")
public class SensorRecentDataVO {
    @Schema(description = "数据ID", example = "1001")
    private Long dataId;
    @Schema(description = "传感器ID", example = "1")
    private Integer sensorId;
    @Schema(description = "采集时间", example = "2026-06-28T09:30:00")
    private LocalDateTime collectTime;
    @Schema(description = "温度", example = "26.5")
    private BigDecimal temperature;
    @Schema(description = "湿度", example = "61.2")
    private BigDecimal humidity;
    @Schema(description = "土壤湿度", example = "42.8")
    private BigDecimal soilMoisture;
    @Schema(description = "光照强度", example = "1280.0")
    private BigDecimal lightIntensity;
}
