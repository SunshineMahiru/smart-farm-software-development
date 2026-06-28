package com.smartfarm.modules.farm.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FarmLogVO {

    private Long logId;
    private Long planId;
    private String plotName;
    private String cropName;
    private Long operatorId;
    private String operatorName;
    private String operationType;
    private LocalDate operationDate;
    private String description;
    private LocalDateTime createTime;
}
