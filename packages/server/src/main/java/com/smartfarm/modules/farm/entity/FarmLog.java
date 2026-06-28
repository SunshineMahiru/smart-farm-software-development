package com.smartfarm.modules.farm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("farm_log")
public class FarmLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long planId;

    private Long operatorId;

    private String operationType;

    private LocalDate operationDate;

    private String description;

    private LocalDateTime createTime;
}
