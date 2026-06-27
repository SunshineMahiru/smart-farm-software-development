package com.smartfarm.modules.iot.service;

import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorHistoryTrendVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;

import java.util.List;

public interface IotDashboardService {

    IotOverviewVO getOverview();

    List<SensorRecentDataVO> getRecentData(Integer sensorId, Integer limit);

    List<DeviceAlertVO> getLatestAlerts(Integer limit);

    SensorHistoryTrendVO getHistoryTrend(Long sensorId, Integer hours);
}
