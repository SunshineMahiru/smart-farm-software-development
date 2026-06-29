package com.smartfarm.modules.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "种植计划日历视图")
public class PlanCalendarVO {

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "地块名称", example = "A区1号棚")
    private String plotName;

    @Schema(description = "作物名称", example = "番茄")
    private String cropName;

    @Schema(description = "计划状态", example = "进行中")
    private String status;

    @Schema(description = "播种日期", example = "2026-07-01")
    private LocalDate startDate;

    @Schema(description = "预计采收日期", example = "2026-09-29")
    private LocalDate expectedHarvest;

    @Schema(description = "距采收剩余天数", example = "32")
    private Long daysToHarvest;
}
