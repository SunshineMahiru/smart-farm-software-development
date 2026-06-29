package com.smartfarm.modules.plan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "作物下拉选项")
public class CropOptionVO {

    @Schema(description = "作物ID", example = "1")
    private Long cropId;

    @Schema(description = "作物名称", example = "番茄")
    private String cropName;

    @Schema(description = "作物类别", example = "茄果类")
    private String category;

    @Schema(description = "生长周期天数", example = "90")
    private Integer growthCycleDays;
}
