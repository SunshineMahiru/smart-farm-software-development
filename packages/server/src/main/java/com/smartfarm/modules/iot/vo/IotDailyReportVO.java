package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IotDailyReportVO {
    private Long reportId;
    private LocalDate reportDate;
    private LocalDateTime generatedAt;
    private LocalDateTime dataStartTime;
    private LocalDateTime dataEndTime;
    private BigDecimal avgTemperature;
    private BigDecimal minTemperature;
    private BigDecimal maxTemperature;
    private BigDecimal avgHumidity;
    private BigDecimal minHumidity;
    private BigDecimal maxHumidity;
    private BigDecimal avgSoilMoisture;
    private BigDecimal avgLightIntensity;
    private Integer totalAlerts;
    private Integer pendingAlerts;
    private String reportContent;
}
