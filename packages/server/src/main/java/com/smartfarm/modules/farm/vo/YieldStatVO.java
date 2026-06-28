package com.smartfarm.modules.farm.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class YieldStatVO {

    private Long yieldId;
    private Long planId;
    private String plotName;
    private String cropName;
    private LocalDate harvestDate;
    private BigDecimal yieldWeight;
    private String qualityGrade;
    private LocalDateTime createTime;
}
