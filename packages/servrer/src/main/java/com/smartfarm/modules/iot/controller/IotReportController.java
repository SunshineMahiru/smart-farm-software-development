package com.smartfarm.modules.iot.controller;

import com.smartfarm.common.utils.Result;
import com.smartfarm.modules.iot.service.IotReportService;
import com.smartfarm.modules.iot.vo.IotDailyReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/iot/reports")
public class IotReportController {

    private final IotReportService iotReportService;

    @GetMapping("/latest")
    public Result<IotDailyReportVO> getLatestReport() {
        return Result.success(iotReportService.getLatestReport());
    }

    @PostMapping("/generate")
    public Result<IotDailyReportVO> generateReport() {
        return Result.success(iotReportService.generateDailyReport());
    }
}
