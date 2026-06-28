package com.smartfarm.modules.iot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "IoT农情日报")
public class IotDailyReportVO {
    @Schema(description = "日报ID", example = "1")
    private Long reportId;
    @Schema(description = "日报日期", example = "2026-06-28")
    private LocalDate reportDate;
    @Schema(description = "生成时间", example = "2026-06-28T07:00:00")
    private LocalDateTime generatedAt;
    @Schema(description = "统计窗口开始时间", example = "2026-06-27T07:00:00")
    private LocalDateTime dataStartTime;
    @Schema(description = "统计窗口结束时间", example = "2026-06-28T07:00:00")
    private LocalDateTime dataEndTime;
    @Schema(description = "平均温度", example = "25.6")
    private BigDecimal avgTemperature;
    @Schema(description = "最低温度", example = "19.2")
    private BigDecimal minTemperature;
    @Schema(description = "最高温度", example = "31.8")
    private BigDecimal maxTemperature;
    @Schema(description = "平均湿度", example = "63.4")
    private BigDecimal avgHumidity;
    @Schema(description = "最低湿度", example = "48.0")
    private BigDecimal minHumidity;
    @Schema(description = "最高湿度", example = "79.5")
    private BigDecimal maxHumidity;
    @Schema(description = "平均土壤湿度", example = "41.2")
    private BigDecimal avgSoilMoisture;
    @Schema(description = "平均光照强度", example = "1268.4")
    private BigDecimal avgLightIntensity;
    @Schema(description = "告警总数", example = "6")
    private Integer totalAlerts;
    @Schema(description = "未处理告警数", example = "2")
    private Integer pendingAlerts;
    @Schema(description = "日报正文", example = "近24小时棚内环境整体稳定，午后温度略高，建议加强通风。")
    private String reportContent;
}
