package com.smartfarm.modules.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "传感器时序数据写入请求")
public class SensorDataSaveRequest {

    @NotNull(message = "Sensor id cannot be null")
    @Schema(description = "传感器ID", example = "1")
    private Long sensorId;

    @Schema(description = "采集时间，为空时默认当前时间", example = "2026-06-29T10:30:00")
    private LocalDateTime collectTime;

    @Schema(description = "空气温度", example = "31.6")
    private BigDecimal temperature;

    @Schema(description = "空气湿度", example = "58.2")
    private BigDecimal humidity;

    @Schema(description = "土壤湿度", example = "36.4")
    private BigDecimal soilMoisture;

    @Schema(description = "光照强度", example = "3250")
    private BigDecimal lightIntensity;
}
