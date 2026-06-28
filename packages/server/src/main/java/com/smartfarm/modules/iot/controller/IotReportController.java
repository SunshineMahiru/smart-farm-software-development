package com.smartfarm.modules.iot.controller;

import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.service.IotReportService;
import com.smartfarm.modules.iot.vo.IotDailyReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "IoT日报接口")
@RequestMapping("/iot/reports")
public class IotReportController {

    private final IotReportService iotReportService;

    @Operation(summary = "查询最新IoT日报", description = "返回最近一次已生成的农情日报内容和聚合统计结果")
    @GetMapping("/latest")
    public Result<IotDailyReportVO> getLatestReport() {
        return Result.success(iotReportService.getLatestReport());
    }

    @Operation(summary = "手动生成IoT日报", description = "聚合近24小时的传感器与告警数据，生成日报并触发WebSocket广播")
    @PostMapping("/generate")
    public Result<IotDailyReportVO> generateReport() {
        return Result.success(iotReportService.generateDailyReport());
    }
}
