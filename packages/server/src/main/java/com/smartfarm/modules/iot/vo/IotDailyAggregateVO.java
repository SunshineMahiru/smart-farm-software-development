package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IotDailyAggregateVO {
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
}
