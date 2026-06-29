package com.smartfarm.modules.iot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "设备告警状态更新请求")
public class DeviceAlertStatusUpdateRequest {

    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "未处理|已处理", message = "Status must be 未处理 or 已处理")
    @Schema(description = "告警状态", example = "已处理")
    private String status;
}
