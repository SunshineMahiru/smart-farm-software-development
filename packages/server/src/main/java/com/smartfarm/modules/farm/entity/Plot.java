package com.smartfarm.modules.farm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("plot")
@Schema(description = "地块台账实体")
public class Plot {

    @Schema(description = "地块ID", example = "1")
    @TableId(value = "plot_id", type = IdType.AUTO)
    private Long plotId;

    @Schema(description = "地块名称", example = "A区1号棚")
    private String plotName;

    @Schema(description = "地块面积", example = "120.50")
    private BigDecimal area;

    @Schema(description = "土壤类型", example = "壤土")
    private String soilType;

    @Schema(description = "位置", example = "农场东区")
    private String location;

    @Schema(description = "地块状态", example = "空闲")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
