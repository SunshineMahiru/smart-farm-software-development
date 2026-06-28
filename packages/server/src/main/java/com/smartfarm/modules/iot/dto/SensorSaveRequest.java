package com.smartfarm.modules.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "传感器新增/修改请求")
public class SensorSaveRequest {

    @NotNull(message = "Plot id cannot be null")
    @Schema(description = "所属地块ID", example = "1")
    private Long plotId;

    @NotBlank(message = "Sensor name cannot be empty")
    @Size(max = 50, message = "Sensor name length cannot exceed 50")
    @Schema(description = "传感器名称", example = "1号棚温湿度传感器")
    private String sensorName;

    @NotBlank(message = "Sensor type cannot be empty")
    @Schema(description = "传感器类型", example = "温湿度传感器")
    private String sensorType;

    @Schema(description = "安装日期", example = "2026-06-28")
    private LocalDate installDate;

    @NotBlank(message = "Sensor status cannot be empty")
    @Schema(description = "传感器状态", example = "在线")
    private String status;
}
