package com.smartfarm.modules.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "作物保存请求")
public class CropSaveRequest {

    @NotBlank(message = "Crop name cannot be blank")
    @Schema(description = "作物名称", example = "番茄")
    private String cropName;

    @NotBlank(message = "Category cannot be blank")
    @Schema(description = "作物类别", example = "茄果类")
    private String category;

    @NotNull(message = "Growth cycle cannot be null")
    @Min(value = 1, message = "Growth cycle must be greater than 0")
    @Schema(description = "生长周期天数", example = "90")
    private Integer growthCycleDays;

    @Schema(description = "适宜温度", example = "24.50")
    private BigDecimal idealTemp;

    @Schema(description = "适宜湿度", example = "68.00")
    private BigDecimal idealHumidity;
}
