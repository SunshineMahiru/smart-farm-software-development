package com.smartfarm.modules.iot.service;

import com.smartfarm.modules.iot.vo.IotDailyReportVO;

public interface IotReportService {

    IotDailyReportVO generateDailyReport();

    IotDailyReportVO getLatestReport();
}
