package com.smartfarm.modules.farm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("yield_stat")
public class YieldStat {

    @TableId(value = "yield_id", type = IdType.AUTO)
    private Long yieldId;

    private Long planId;

    private LocalDate harvestDate;

    private BigDecimal yieldWeight;

    private String qualityGrade;

    private LocalDateTime createTime;
}
