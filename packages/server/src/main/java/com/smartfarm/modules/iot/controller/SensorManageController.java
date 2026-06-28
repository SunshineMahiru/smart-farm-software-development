package com.smartfarm.modules.iot.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.dto.SensorSaveRequest;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.service.SensorManageService;
import com.smartfarm.modules.iot.vo.SensorOnlineStatusVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "IoT传感器管理接口")
@RequestMapping("/iot/sensors")
public class SensorManageController {

    private final SensorManageService sensorManageService;

    @Operation(summary = "分页查询传感器", description = "支持按地块、状态、类型和关键字筛选传感器列表")
    @GetMapping
    public Result<IPage<Sensor>> pageSensors(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "地块ID", example = "1")
            @RequestParam(required = false) Long plotId,
            @Parameter(description = "设备状态，如在线、离线", example = "在线")
            @RequestParam(required = false) String status,
            @Parameter(description = "传感器类型", example = "温湿度传感器")
            @RequestParam(required = false) String sensorType,
            @Parameter(description = "关键字，匹配传感器名称或类型", example = "温湿")
            @RequestParam(required = false) String keyword) {
        return Result.success(sensorManageService.pageSensors(pageNum, pageSize, plotId, status, sensorType, keyword));
    }

    @Operation(summary = "查询传感器详情", description = "根据传感器ID查询单个传感器详情")
    @GetMapping("/{sensorId}")
    public Result<Sensor> getSensor(@Parameter(description = "传感器ID", example = "1") @PathVariable Long sensorId) {
        return Result.success(sensorManageService.getSensorById(sensorId));
    }

    @Operation(summary = "分页查询传感器在线状态", description = "返回传感器当前状态及最近一次采集时间、温湿度")
    @GetMapping("/online-status")
    public Result<IPage<SensorOnlineStatusVO>> pageOnlineStatus(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "状态筛选，如在线、离线", example = "在线")
            @RequestParam(required = false) String status) {
        return Result.success(sensorManageService.pageOnlineStatus(pageNum, pageSize, status));
    }

    @SaCheckRole("管理员")
    @Operation(summary = "新增传感器", description = "创建一条新的传感器记录，要求传感器名称唯一")
    @PostMapping
    public Result<String> createSensor(@Valid @RequestBody SensorSaveRequest request) {
        sensorManageService.createSensor(request);
        return Result.success("Sensor created successfully");
    }

    @SaCheckRole("管理员")
    @Operation(summary = "修改传感器", description = "根据传感器ID更新传感器基础信息")
    @PutMapping("/{sensorId}")
    public Result<String> updateSensor(
            @Parameter(description = "传感器ID", example = "1") @PathVariable Long sensorId,
            @Valid @RequestBody SensorSaveRequest request) {
        sensorManageService.updateSensor(sensorId, request);
        return Result.success("Sensor updated successfully");
    }

    @SaCheckRole("管理员")
    @Operation(summary = "删除传感器", description = "根据传感器ID删除传感器记录")
    @DeleteMapping("/{sensorId}")
    public Result<String> deleteSensor(@Parameter(description = "传感器ID", example = "1") @PathVariable Long sensorId) {
        sensorManageService.deleteSensor(sensorId);
        return Result.success("Sensor deleted successfully");
    }
}
