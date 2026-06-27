package com.smartfarm.modules.iot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DeviceAlertVO {
    private Integer alertId;
    private Integer plotId;
    private String plotName;
    private LocalDateTime alertTime;
    private String alertType;
    private BigDecimal alertValue;
    private String status;
}
