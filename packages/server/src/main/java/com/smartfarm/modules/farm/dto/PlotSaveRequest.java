package com.smartfarm.modules.farm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "地块保存请求")
public class PlotSaveRequest {

    @Schema(description = "地块名称，唯一", example = "A区1号棚")
    @NotBlank(message = "Plot name cannot be empty")
    @Size(max = 50, message = "Plot name length cannot exceed 50")
    private String plotName;

    @Schema(description = "地块面积", example = "120.50")
    @NotNull(message = "Area cannot be empty")
    @DecimalMin(value = "0.01", message = "Area must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Area precision is invalid")
    private BigDecimal area;

    @Schema(description = "土壤类型", example = "壤土")
    @Size(max = 50, message = "Soil type length cannot exceed 50")
    private String soilType;

    @Schema(description = "位置", example = "农场东区")
    @Size(max = 100, message = "Location length cannot exceed 100")
    private String location;

    @Schema(description = "地块状态，只允许空闲、种植中、休耕、维护中", example = "空闲")
    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "空闲|种植中|休耕|维护中", message = "Status must be 空闲, 种植中, 休耕 or 维护中")
    private String status;
}
