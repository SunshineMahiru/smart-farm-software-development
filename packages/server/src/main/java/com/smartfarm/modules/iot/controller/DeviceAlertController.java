package com.smartfarm.modules.iot.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.dto.DeviceAlertStatusUpdateRequest;
import com.smartfarm.modules.iot.service.DeviceAlertService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "IoT设备告警管理接口")
@RequestMapping("/iot/alerts")
public class DeviceAlertController {

    private final DeviceAlertService deviceAlertService;

    @SaCheckPermission("sensor:read")
    @Operation(summary = "分页查询设备告警", description = "支持按地块、状态、时间范围和关键字筛选告警记录")
    @GetMapping
    public Result<IPage<DeviceAlertVO>> pageAlerts(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "地块ID", example = "1")
            @RequestParam(required = false) Long plotId,
            @Parameter(description = "告警状态，仅支持未处理或已处理", example = "未处理")
            @RequestParam(required = false) String status,
            @Parameter(description = "开始时间，ISO-8601 格式", example = "2026-06-28T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间，ISO-8601 格式", example = "2026-06-29T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "关键字，匹配告警类型或地块名称", example = "温度")
            @RequestParam(required = false) String keyword) {
        return Result.success(deviceAlertService.pageAlerts(pageNum, pageSize, plotId, status, startTime, endTime, keyword));
    }

    @SaCheckPermission("sensor:read")
    @Operation(summary = "查询告警详情", description = "根据告警ID查看单条告警记录详情")
    @GetMapping("/{alertId}")
    public Result<DeviceAlertVO> getAlert(@Parameter(description = "告警ID", example = "1") @PathVariable Long alertId) {
        return Result.success(deviceAlertService.getAlertById(alertId));
    }

    @SaCheckPermission("sensor:write")
    @Operation(summary = "更新告警状态", description = "用于将告警从未处理标记为已处理，或重新改回未处理")
    @PatchMapping("/{alertId}/status")
    public Result<String> updateAlertStatus(
            @Parameter(description = "告警ID", example = "1") @PathVariable Long alertId,
            @Valid @RequestBody DeviceAlertStatusUpdateRequest request) {
        deviceAlertService.updateAlertStatus(alertId, request.getStatus());
        return Result.success("Device alert status updated successfully");
    }

    @SaCheckPermission("sensor:write")
    @Operation(summary = "删除告警记录", description = "删除一条设备告警记录")
    @DeleteMapping("/{alertId}")
    public Result<String> deleteAlert(@Parameter(description = "告警ID", example = "1") @PathVariable Long alertId) {
        deviceAlertService.deleteAlert(alertId);
        return Result.success("Device alert deleted successfully");
    }
}
