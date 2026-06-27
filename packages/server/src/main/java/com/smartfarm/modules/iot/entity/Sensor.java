package com.smartfarm.modules.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("sensor")
public class Sensor {

    @TableId(value = "sensor_id", type = IdType.AUTO)
    private Long sensorId;

    private Long plotId;

    private String sensorName;

    private String sensorType;

    private LocalDate installDate;

    private String status;

    @TableLogic
    @TableField("deleted")
    private Integer deleted;
}
