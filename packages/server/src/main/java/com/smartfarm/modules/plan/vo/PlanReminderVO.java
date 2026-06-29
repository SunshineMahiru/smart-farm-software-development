package com.smartfarm.modules.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "计划提醒概览")
public class PlanReminderVO {

    @Schema(description = "进行中计划数", example = "8")
    private long ongoingCount;

    @Schema(description = "7天内待采收计划数", example = "2")
    private long upcomingHarvestCount;

    @Schema(description = "今日开始计划数", example = "1")
    private long startTodayCount;

    @Schema(description = "已逾期未完成计划数", example = "0")
    private long overdueCount;
}
