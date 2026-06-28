package com.smartfarm.modules.iot.controller;

import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.service.IotDashboardService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorHistoryTrendVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "IoT大屏与查询接口")
@RequestMapping("/iot")
public class IotDashboardController {

    private final IotDashboardService iotDashboardService;

    @Operation(summary = "获取IoT概览统计", description = "返回传感器在线状态、今日采集量、告警数量和最近采集时间等概览信息")
    @GetMapping("/overview")
    public Result<IotOverviewVO> getOverview() {
        return Result.success(iotDashboardService.getOverview());
    }

    @Operation(summary = "查询传感器最近采集数据", description = "按传感器ID返回最近若干条时序数据，默认20条，最多100条")
    @GetMapping("/recent-data")
    public Result<List<SensorRecentDataVO>> getRecentData(
            @Parameter(description = "传感器ID", example = "1")
            @RequestParam Integer sensorId,
            @Parameter(description = "返回条数上限，范围1-100", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        return Result.success(iotDashboardService.getRecentData(sensorId, limit));
    }

    @Operation(summary = "查询最新告警列表", description = "返回最新设备告警，并附带地块名称，默认10条，最多50条")
    @GetMapping("/alerts/latest")
    public Result<List<DeviceAlertVO>> getLatestAlerts(
            @Parameter(description = "返回告警条数上限，范围1-50", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        return Result.success(iotDashboardService.getLatestAlerts(limit));
    }

    @Operation(summary = "查询传感器历史趋势", description = "返回指定传感器近若干小时的温湿度趋势数据，适用于双Y轴折线图")
    @GetMapping("/history/trend")
    public Result<SensorHistoryTrendVO> getHistoryTrend(
            @Parameter(description = "传感器ID", example = "1")
            @RequestParam @Min(1) Long sensorId,
            @Parameter(description = "查询小时数，范围1-168", example = "24")
            @RequestParam(defaultValue = "24") @Min(1) @Max(168) Integer hours) {
        return Result.success(iotDashboardService.getHistoryTrend(sensorId, hours));
    }
}
