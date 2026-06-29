package com.smartfarm.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("planting_plan")
@Schema(description = "种植计划实体")
public class PlantingPlan {

    @TableId(value = "plan_id", type = IdType.AUTO)
    @Schema(description = "计划ID", example = "1")
    private Long planId;

    @Schema(description = "地块ID", example = "2")
    private Long plotId;

    @Schema(description = "作物ID", example = "3")
    private Long cropId;

    @Schema(description = "播种日期", example = "2026-07-01")
    private LocalDate startDate;

    @Schema(description = "预计采收日期", example = "2026-10-01")
    private LocalDate expectedHarvest;

    @Schema(description = "种植面积", example = "18.50")
    private BigDecimal plantArea;

    @Schema(description = "计划状态", example = "进行中")
    private String status;

    @Schema(description = "创建人ID", example = "1")
    private Long createdBy;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
