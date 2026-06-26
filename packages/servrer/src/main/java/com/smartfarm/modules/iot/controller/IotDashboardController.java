package com.smartfarm.modules.iot.controller;

import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.service.IotDashboardService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
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
@RequestMapping("/iot")
public class IotDashboardController {

    private final IotDashboardService iotDashboardService;

    @GetMapping("/overview")
    public Result<IotOverviewVO> getOverview() {
        return Result.success(iotDashboardService.getOverview());
    }

    @GetMapping("/recent-data")
    public Result<List<SensorRecentDataVO>> getRecentData(
            @RequestParam Integer sensorId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        return Result.success(iotDashboardService.getRecentData(sensorId, limit));
    }

    @GetMapping("/alerts/latest")
    public Result<List<DeviceAlertVO>> getLatestAlerts(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        return Result.success(iotDashboardService.getLatestAlerts(limit));
    }
}
