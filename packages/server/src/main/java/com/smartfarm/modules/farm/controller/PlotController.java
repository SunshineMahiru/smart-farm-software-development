package com.smartfarm.modules.farm.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.farm.dto.PlotSaveRequest;
import com.smartfarm.modules.farm.dto.PlotStatusUpdateRequest;
import com.smartfarm.modules.farm.entity.Plot;
import com.smartfarm.modules.farm.service.PlotService;
import com.smartfarm.modules.farm.vo.PlotStatusSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "成员1-地块台账管理接口")
@RequestMapping("/farm/plots")
public class PlotController {

    private final PlotService plotService;

    @SaCheckPermission("plot:read")
    @Operation(summary = "分页查询地块", description = "支持按关键字、状态、土壤类型和位置筛选地块台账")
    @GetMapping
    public Result<IPage<Plot>> pagePlots(
            @Parameter(description = "页码，从1开始", example = "1")
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @Parameter(description = "关键字，匹配地块名称、位置或土壤类型", example = "A区")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "地块状态", example = "空闲")
            @RequestParam(required = false) String status,
            @Parameter(description = "土壤类型", example = "壤土")
            @RequestParam(required = false) String soilType,
            @Parameter(description = "位置", example = "农场东区")
            @RequestParam(required = false) String location) {
        return Result.success(plotService.pagePlots(pageNum, pageSize, keyword, status, soilType, location));
    }

    @SaCheckPermission("plot:read")
    @Operation(summary = "查询地块详情", description = "根据地块ID查询台账详情")
    @GetMapping("/{plotId}")
    public Result<Plot> getPlot(@Parameter(description = "地块ID", example = "1") @PathVariable Long plotId) {
        return Result.success(plotService.getPlotById(plotId));
    }

    @SaCheckPermission("plot:read")
    @Operation(summary = "地块状态汇总", description = "按地块状态统计数量和面积")
    @GetMapping("/status-summary")
    public Result<List<PlotStatusSummaryVO>> getStatusSummary() {
        return Result.success(plotService.getStatusSummary());
    }

    @SaCheckPermission("plot:write")
    @Operation(summary = "新增地块", description = "创建地块台账，地块名称必须唯一")
    @PostMapping
    public Result<String> createPlot(@Valid @RequestBody PlotSaveRequest request) {
        plotService.createPlot(request);
        return Result.success("Plot created successfully");
    }

    @SaCheckPermission("plot:write")
    @Operation(summary = "修改地块", description = "更新地块基础信息，状态变更会走状态流转校验")
    @PutMapping("/{plotId}")
    public Result<String> updatePlot(
            @Parameter(description = "地块ID", example = "1") @PathVariable Long plotId,
            @Valid @RequestBody PlotSaveRequest request) {
        plotService.updatePlot(plotId, request);
        return Result.success("Plot updated successfully");
    }

    @SaCheckPermission("plot:write")
    @Operation(summary = "地块状态流转", description = "执行地块状态流转，如空闲到种植中")
    @PatchMapping("/{plotId}/status")
    public Result<String> updatePlotStatus(
            @Parameter(description = "地块ID", example = "1") @PathVariable Long plotId,
            @Valid @RequestBody PlotStatusUpdateRequest request) {
        plotService.updatePlotStatus(plotId, request.getStatus());
        return Result.success("Plot status updated successfully");
    }

    @SaCheckPermission("plot:write")
    @Operation(summary = "删除地块", description = "删除未被业务记录引用的地块")
    @DeleteMapping("/{plotId}")
    public Result<String> deletePlot(@Parameter(description = "地块ID", example = "1") @PathVariable Long plotId) {
        plotService.deletePlot(plotId);
        return Result.success("Plot deleted successfully");
    }
}
