package com.smartfarm.modules.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Schema(description = "种植计划保存请求")
public class PlantingPlanSaveRequest {

    @NotNull(message = "Plot ID cannot be null")
    @Schema(description = "地块ID", example = "1")
    private Long plotId;

    @NotNull(message = "Crop ID cannot be null")
    @Schema(description = "作物ID", example = "2")
    private Long cropId;

    @NotNull(message = "Start date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "播种日期", example = "2026-07-01")
    private LocalDate startDate;

    @NotNull(message = "Plant area cannot be null")
    @Min(value = 1, message = "Plant area must be greater than 0")
    @Schema(description = "种植面积", example = "16.80")
    private BigDecimal plantArea;

    @NotNull(message = "Creator ID cannot be null")
    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "计划状态，仅支持传入“已取消”覆盖自动状态", example = "未开始")
    private String status;
}
