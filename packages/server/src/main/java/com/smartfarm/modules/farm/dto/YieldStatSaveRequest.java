package com.smartfarm.modules.farm.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class YieldStatSaveRequest {

    @NotNull(message = "Planting plan id is required")
    private Long planId;

    private LocalDate harvestDate;

    @NotNull(message = "Yield weight is required")
    @DecimalMin(value = "0.0", message = "Yield weight cannot be negative")
    private BigDecimal yieldWeight;

    private String qualityGrade;
}
