package com.smartfarm.modules.farm.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PlanYieldSummaryVO {

    private Long planId;
    private String plotName;
    private String cropName;
    private Long recordCount;
    private BigDecimal totalYield;
    private BigDecimal avgYield;
    private LocalDate latestHarvestDate;
}
