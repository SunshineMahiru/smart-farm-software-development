package com.smartfarm.modules.farm.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.farm.dto.FarmLogSaveRequest;
import com.smartfarm.modules.farm.service.FarmLogService;
import com.smartfarm.modules.farm.vo.FarmLogVO;
import com.smartfarm.modules.farm.vo.LogSummaryVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/farm/logs")
public class FarmLogController {

    private final FarmLogService farmLogService;

    @GetMapping
    public Result<IPage<FarmLogVO>> pageLogs(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(farmLogService.pageLogs(pageNum, pageSize, planId, operatorId, operationType, startDate, endDate));
    }

    @GetMapping("/{logId}")
    public Result<FarmLogVO> getLog(@PathVariable Long logId) {
        return Result.success(farmLogService.getLogById(logId));
    }

    @GetMapping("/summary")
    public Result<List<LogSummaryVO>> getLogSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(farmLogService.getLogSummary(startDate, endDate));
    }

    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createLog(@Valid @RequestBody FarmLogSaveRequest request) {
        farmLogService.createLog(request);
        return Result.success("Farm log created successfully");
    }

    @SaCheckRole("管理员")
    @PutMapping("/{logId}")
    public Result<String> updateLog(@PathVariable Long logId, @Valid @RequestBody FarmLogSaveRequest request) {
        farmLogService.updateLog(logId, request);
        return Result.success("Farm log updated successfully");
    }

    @SaCheckRole("管理员")
    @DeleteMapping("/{logId}")
    public Result<String> deleteLog(@PathVariable Long logId) {
        farmLogService.deleteLog(logId);
        return Result.success("Farm log deleted successfully");
    }
}
