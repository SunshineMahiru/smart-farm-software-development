package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "传感器历史趋势点")
public class SensorHistoryPointVO {
    @Schema(description = "采集时间", example = "2026-06-28T09:00:00")
    private LocalDateTime collectTime;
    @Schema(description = "温度", example = "26.3")
    private BigDecimal temperature;
    @Schema(description = "湿度", example = "60.5")
    private BigDecimal humidity;
}
