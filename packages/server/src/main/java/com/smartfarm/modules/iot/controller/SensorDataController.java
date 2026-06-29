package com.smartfarm.modules.iot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.dto.SensorDataSaveRequest;
import com.smartfarm.modules.iot.service.SensorDataService;
import com.smartfarm.modules.iot.vo.SensorDataPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "IoT时序数据接口")
@RequestMapping("/iot/sensor-data")
public class SensorDataController {

    private final SensorDataService sensorDataService;

    @SaCheckPermission("sensor:read")
    @Operation(summary = "分页查询时序数据", description = "支持按传感器、地块和时间范围筛选时序数据，用于管理页和数据联调")
    @GetMapping
    public Result<IPage<SensorDataPageVO>> pageSensorData(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "20")
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "传感器ID", example = "1")
            @RequestParam(required = false) Long sensorId,
            @Parameter(description = "地块ID", example = "1")
            @RequestParam(required = false) Long plotId,
            @Parameter(description = "开始时间，ISO-8601 格式", example = "2026-06-28T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间，ISO-8601 格式", example = "2026-06-29T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.success(sensorDataService.pageSensorData(pageNum, pageSize, sensorId, plotId, startTime, endTime));
    }

    @SaCheckPermission("sensor:write")
    @Operation(summary = "写入传感器数据", description = "写入一条时序数据，并按阈值自动生成设备告警和 WebSocket 广播")
    @PostMapping
    public Result<String> createSensorData(@Valid @RequestBody SensorDataSaveRequest request) {
        sensorDataService.createSensorData(request);
        return Result.success("Sensor data created successfully");
    }
}
