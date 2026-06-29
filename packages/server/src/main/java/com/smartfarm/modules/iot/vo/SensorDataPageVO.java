package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "传感器时序数据分页视图")
public class SensorDataPageVO {

    @Schema(description = "数据ID", example = "28001")
    private Long dataId;

    @Schema(description = "传感器ID", example = "1")
    private Long sensorId;

    @Schema(description = "传感器名称", example = "1号棚温度传感器")
    private String sensorName;

    @Schema(description = "传感器类型", example = "温度")
    private String sensorType;

    @Schema(description = "地块ID", example = "1")
    private Long plotId;

    @Schema(description = "地块名称", example = "东区1号棚")
    private String plotName;

    @Schema(description = "采集时间", example = "2026-06-29T10:30:00")
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
