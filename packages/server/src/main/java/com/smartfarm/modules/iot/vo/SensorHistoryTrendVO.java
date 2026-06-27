package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SensorHistoryTrendVO {
    private Long sensorId;
    private String sensorName;
    private String sensorType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<SensorHistoryPointVO> points;
}
