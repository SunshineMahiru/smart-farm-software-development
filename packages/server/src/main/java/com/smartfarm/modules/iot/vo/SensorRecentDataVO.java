package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SensorRecentDataVO {
    private Long dataId;
    private Integer sensorId;
    private LocalDateTime collectTime;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal soilMoisture;
    private BigDecimal lightIntensity;
}
