package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "设备告警信息")
public class DeviceAlertVO {
    @Schema(description = "告警ID", example = "20")
    private Integer alertId;
    @Schema(description = "地块ID", example = "3")
    private Integer plotId;
    @Schema(description = "地块名称", example = "东区1号棚")
    private String plotName;
    @Schema(description = "告警时间", example = "2026-06-28T08:15:00")
    private LocalDateTime alertTime;
    @Schema(description = "告警类型", example = "温度过高")
    private String alertType;
    @Schema(description = "告警值", example = "38.6")
    private BigDecimal alertValue;
    @Schema(description = "处理状态", example = "未处理")
    private String status;
}
