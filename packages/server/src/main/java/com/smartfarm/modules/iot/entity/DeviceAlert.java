package com.smartfarm.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("device_alert")
public class DeviceAlert {

    @TableId(value = "alert_id", type = IdType.AUTO)
    private Long alertId;

    private Long plotId;

    private LocalDateTime alertTime;

    private String alertType;

    private BigDecimal alertValue;

    private String status;
}
