package com.smartfarm.modules.farm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "地块状态流转请求")
public class PlotStatusUpdateRequest {

    @Schema(description = "目标状态，只允许空闲、种植中、休耕、维护中", example = "种植中")
    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "空闲|种植中|休耕|维护中", message = "Status must be 空闲, 种植中, 休耕 or 维护中")
    private String status;
}
