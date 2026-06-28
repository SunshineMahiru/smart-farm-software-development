package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "传感器在线状态视图")
public class SensorOnlineStatusVO {

    @Schema(description = "传感器ID", example = "1")
    private Long sensorId;

    @Schema(description = "地块ID", example = "1")
    private Long plotId;

    @Schema(description = "地块名称", example = "东区1号棚")
    private String plotName;

    @Schema(description = "传感器名称", example = "1号棚温湿度传感器")
    private String sensorName;

    @Schema(description = "传感器类型", example = "温湿度传感器")
    private String sensorType;

    @Schema(description = "在线状态", example = "在线")
    private String status;

    @Schema(description = "最近采集时间", example = "2026-06-28T09:30:00")
    private LocalDateTime latestCollectTime;

    @Schema(description = "最近一次温度", example = "26.4")
    private Double latestTemperature;

    @Schema(description = "最近一次湿度", example = "60.8")
    private Double latestHumidity;
}
