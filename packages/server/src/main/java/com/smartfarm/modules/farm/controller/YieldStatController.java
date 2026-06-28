package com.smartfarm.modules.farm.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.farm.dto.YieldStatSaveRequest;
import com.smartfarm.modules.farm.service.YieldStatService;
import com.smartfarm.modules.farm.vo.PlanYieldSummaryVO;
import com.smartfarm.modules.farm.vo.YieldStatVO;
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
@RequestMapping("/farm/yields")
public class YieldStatController {

    private final YieldStatService yieldStatService;

    @GetMapping
    public Result<IPage<YieldStatVO>> pageYieldStats(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) Long planId,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.success(yieldStatService.pageYieldStats(pageNum, pageSize, planId, qualityGrade, startDate, endDate));
    }

    @GetMapping("/{yieldId}")
    public Result<YieldStatVO> getYield(@PathVariable Long yieldId) {
        return Result.success(yieldStatService.getYieldById(yieldId));
    }

    @GetMapping("/summary")
    public Result<List<PlanYieldSummaryVO>> getPlanYieldSummary(@RequestParam(required = false) Long planId) {
        return Result.success(yieldStatService.getPlanYieldSummary(planId));
    }

    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createYield(@Valid @RequestBody YieldStatSaveRequest request) {
        yieldStatService.createYield(request);
        return Result.success("Yield stat created successfully");
    }

    @SaCheckRole("管理员")
    @PutMapping("/{yieldId}")
    public Result<String> updateYield(@PathVariable Long yieldId, @Valid @RequestBody YieldStatSaveRequest request) {
        yieldStatService.updateYield(yieldId, request);
        return Result.success("Yield stat updated successfully");
    }

    @SaCheckRole("管理员")
    @DeleteMapping("/{yieldId}")
    public Result<String> deleteYield(@PathVariable Long yieldId) {
        yieldStatService.deleteYield(yieldId);
        return Result.success("Yield stat deleted successfully");
    }
}
