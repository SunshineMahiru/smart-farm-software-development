package com.smartfarm.modules.farm.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "地块状态汇总")
public class PlotStatusSummaryVO {

    @Schema(description = "地块状态", example = "种植中")
    private String status;

    @Schema(description = "地块数量", example = "8")
    private Long totalCount;

    @Schema(description = "状态对应总面积", example = "960.50")
    private BigDecimal totalArea;
}
