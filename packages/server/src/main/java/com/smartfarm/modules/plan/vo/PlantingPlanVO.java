package com.smartfarm.modules.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "种植计划展示对象")
public class PlantingPlanVO {

    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "地块ID", example = "1")
    private Long plotId;

    @Schema(description = "地块名称", example = "A区1号棚")
    private String plotName;

    @Schema(description = "作物ID", example = "2")
    private Long cropId;

    @Schema(description = "作物名称", example = "番茄")
    private String cropName;

    @Schema(description = "作物类别", example = "茄果类")
    private String category;

    @Schema(description = "生长周期天数", example = "90")
    private Integer growthCycleDays;

    @Schema(description = "播种日期", example = "2026-07-01")
    private LocalDate startDate;

    @Schema(description = "预计采收日期", example = "2026-09-29")
    private LocalDate expectedHarvest;

    @Schema(description = "种植面积", example = "16.50")
    private BigDecimal plantArea;

    @Schema(description = "计划状态", example = "进行中")
    private String status;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建人姓名", example = "管理员")
    private String creatorName;

    @Schema(description = "距采收剩余天数", example = "32")
    private Long daysToHarvest;

    @Schema(description = "进度百分比", example = "64")
    private Integer progressPercent;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
