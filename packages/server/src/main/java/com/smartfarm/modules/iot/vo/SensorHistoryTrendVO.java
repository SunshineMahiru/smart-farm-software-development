package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "传感器历史趋势")
public class SensorHistoryTrendVO {
    @Schema(description = "传感器ID", example = "1")
    private Long sensorId;
    @Schema(description = "传感器名称", example = "1号棚温湿度传感器")
    private String sensorName;
    @Schema(description = "传感器类型", example = "温湿度传感器")
    private String sensorType;
    @Schema(description = "趋势起始时间", example = "2026-06-27T09:00:00")
    private LocalDateTime startTime;
    @Schema(description = "趋势结束时间", example = "2026-06-28T09:00:00")
    private LocalDateTime endTime;
    @Schema(description = "按时间升序排列的趋势点")
    private List<SensorHistoryPointVO> points;
}
