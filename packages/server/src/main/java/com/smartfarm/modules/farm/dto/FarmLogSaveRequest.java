package com.smartfarm.modules.farm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FarmLogSaveRequest {

    @NotNull(message = "Planting plan id is required")
    private Long planId;

    @NotNull(message = "Operator id is required")
    private Long operatorId;

    @NotBlank(message = "Operation type is required")
    private String operationType;

    @NotNull(message = "Operation date is required")
    private LocalDate operationDate;

    private String description;
}
