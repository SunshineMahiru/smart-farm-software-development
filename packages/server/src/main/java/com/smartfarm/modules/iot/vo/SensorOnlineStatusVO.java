package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensorOnlineStatusVO {

    private Long sensorId;

    private Long plotId;

    private String plotName;

    private String sensorName;

    private String sensorType;

    private String status;

    private LocalDateTime latestCollectTime;

    private Double latestTemperature;

    private Double latestHumidity;
}
