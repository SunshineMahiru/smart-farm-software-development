package com.smartfarm.modules.plan.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("crop")
@Schema(description = "作物字典实体")
public class Crop {

    @TableId(value = "crop_id", type = IdType.AUTO)
    @Schema(description = "作物ID", example = "1")
    private Long cropId;

    @Schema(description = "作物名称", example = "番茄")
    private String cropName;

    @Schema(description = "作物类别", example = "茄果类")
    private String category;

    @Schema(description = "生长周期天数", example = "90")
    private Integer growthCycleDays;

    @Schema(description = "适宜温度", example = "24.50")
    private BigDecimal idealTemp;

    @Schema(description = "适宜湿度", example = "68.00")
    private BigDecimal idealHumidity;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
