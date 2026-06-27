package com.smartfarm.modules.iot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SensorSaveRequest {

    @NotNull(message = "Plot id cannot be null")
    private Long plotId;

    @NotBlank(message = "Sensor name cannot be empty")
    @Size(max = 50, message = "Sensor name length cannot exceed 50")
    private String sensorName;

    @NotBlank(message = "Sensor type cannot be empty")
    private String sensorType;

    private LocalDate installDate;

    @NotBlank(message = "Sensor status cannot be empty")
    private String status;
}
