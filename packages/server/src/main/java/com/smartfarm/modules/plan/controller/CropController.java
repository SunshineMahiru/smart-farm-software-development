package com.smartfarm.modules.plan.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.plan.dto.CropSaveRequest;
import com.smartfarm.modules.plan.entity.Crop;
import com.smartfarm.modules.plan.service.CropService;
import com.smartfarm.modules.plan.vo.CropOptionVO;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@Tag(name = "成员2-作物字典")
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/plan/crops")
public class CropController {

    private final CropService cropService;

    @Operation(summary = "分页查询作物字典")
    @GetMapping
    public Result<IPage<Crop>> pageCrops(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page number must be greater than 0") long pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be greater than 0") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        return Result.success(cropService.pageCrops(pageNum, pageSize, keyword, category));
    }

    @Operation(summary = "查询作物下拉选项")
    @GetMapping("/options")
    public Result<List<CropOptionVO>> listOptions() {
        return Result.success(cropService.listOptions());
    }

    @Operation(summary = "查询作物详情")
    @GetMapping("/{cropId}")
    public Result<Crop> getCrop(@PathVariable Long cropId) {
        return Result.success(cropService.getCropById(cropId));
    }

    @Operation(summary = "创建作物")
    @SaCheckRole("管理员")
    @PostMapping
    public Result<String> createCrop(@Valid @RequestBody CropSaveRequest request) {
        cropService.createCrop(request);
        return Result.success("Crop created successfully");
    }

    @Operation(summary = "更新作物")
    @SaCheckRole("管理员")
    @PutMapping("/{cropId}")
    public Result<String> updateCrop(@PathVariable Long cropId, @Valid @RequestBody CropSaveRequest request) {
        cropService.updateCrop(cropId, request);
        return Result.success("Crop updated successfully");
    }

    @Operation(summary = "删除作物")
    @SaCheckRole("管理员")
    @DeleteMapping("/{cropId}")
    public Result<String> deleteCrop(@PathVariable Long cropId) {
        cropService.deleteCrop(cropId);
        return Result.success("Crop deleted successfully");
    }
}
