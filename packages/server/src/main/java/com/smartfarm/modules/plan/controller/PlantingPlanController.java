package com.smartfarm.modules.plan.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.plan.dto.PlantingPlanSaveRequest;
import com.smartfarm.modules.plan.service.PlantingPlanService;
import com.smartfarm.modules.plan.vo.PlanCalendarVO;
import com.smartfarm.modules.plan.vo.PlanReminderVO;
import com.smartfarm.modules.plan.vo.PlantingPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "成员2-种植计划")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/plan/plans")
public class PlantingPlanController {

    private final PlantingPlanService plantingPlanService;

    @Operation(summary = "分页查询种植计划")
    @GetMapping
    public Result<IPage<PlantingPlanVO>> pagePlans(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) Long plotId,
            @RequestParam(required = false) Long cropId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(plantingPlanService.pagePlans(pageNum, pageSize, plotId, cropId, status, keyword, startDate, endDate));
    }

    @Operation(summary = "查询种植计划详情")
    @GetMapping("/{planId}")
    public Result<PlantingPlanVO> getPlan(@PathVariable Long planId) {
        return Result.success(plantingPlanService.getPlanById(planId));
    }

    @Operation(summary = "查询种植计划日历视图")
    @GetMapping("/calendar")
    public Result<List<PlanCalendarVO>> calendarPlans(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(plantingPlanService.listCalendarPlans(startDate, endDate));
    }

    @Operation(summary = "查询计划提醒概览")
    @GetMapping("/reminders")
    public Result<PlanReminderVO> reminderSummary() {
        return Result.success(plantingPlanService.getReminderSummary());
    }

    @Operation(summary = "创建种植计划")
    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createPlan(@Valid @RequestBody PlantingPlanSaveRequest request) {
        plantingPlanService.createPlan(request);
        return Result.success("Planting plan created successfully");
    }

    @Operation(summary = "更新种植计划")
    @SaCheckRole("管理员")
    @PutMapping("/{planId}")
    public Result<String> updatePlan(@PathVariable Long planId, @Valid @RequestBody PlantingPlanSaveRequest request) {
        plantingPlanService.updatePlan(planId, request);
        return Result.success("Planting plan updated successfully");
    }

    @Operation(summary = "删除种植计划")
    @SaCheckRole("管理员")
    @DeleteMapping("/{planId}")
    public Result<String> deletePlan(@PathVariable Long planId) {
        plantingPlanService.deletePlan(planId);
        return Result.success("Planting plan deleted successfully");
    }
}
