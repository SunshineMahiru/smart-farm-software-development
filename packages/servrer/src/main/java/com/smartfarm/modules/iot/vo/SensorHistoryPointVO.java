package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SensorHistoryPointVO {
    private LocalDateTime collectTime;
    private BigDecimal temperature;
    private BigDecimal humidity;
}
