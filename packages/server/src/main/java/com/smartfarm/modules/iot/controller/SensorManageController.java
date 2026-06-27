package com.smartfarm.modules.iot.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.dto.SensorSaveRequest;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.service.SensorManageService;
import com.smartfarm.modules.iot.vo.SensorOnlineStatusVO;
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
@RequestMapping("/iot/sensors")
public class SensorManageController {

    private final SensorManageService sensorManageService;

    @GetMapping
    public Result<IPage<Sensor>> pageSensors(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) Long plotId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sensorType,
            @RequestParam(required = false) String keyword) {
        return Result.success(sensorManageService.pageSensors(pageNum, pageSize, plotId, status, sensorType, keyword));
    }

    @GetMapping("/{sensorId}")
    public Result<Sensor> getSensor(@PathVariable Long sensorId) {
        return Result.success(sensorManageService.getSensorById(sensorId));
    }

    @GetMapping("/online-status")
    public Result<IPage<SensorOnlineStatusVO>> pageOnlineStatus(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(sensorManageService.pageOnlineStatus(pageNum, pageSize, status));
    }

    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createSensor(@Valid @RequestBody SensorSaveRequest request) {
        sensorManageService.createSensor(request);
        return Result.success("Sensor created successfully");
    }

    @SaCheckRole("管理员")
    @PutMapping("/{sensorId}")
    public Result<String> updateSensor(@PathVariable Long sensorId, @Valid @RequestBody SensorSaveRequest request) {
        sensorManageService.updateSensor(sensorId, request);
        return Result.success("Sensor updated successfully");
    }

    @SaCheckRole("管理员")
    @DeleteMapping("/{sensorId}")
    public Result<String> deleteSensor(@PathVariable Long sensorId) {
        sensorManageService.deleteSensor(sensorId);
        return Result.success("Sensor deleted successfully");
    }
}
