package com.smartfarm.modules.iot.service.impl;

import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.iot.mapper.IotDashboardMapper;
import com.smartfarm.modules.iot.service.IotDashboardService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorHistoryTrendVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IotDashboardServiceImpl implements IotDashboardService {

    private final IotDashboardMapper iotDashboardMapper;

    @Override
    public IotOverviewVO getOverview() {
        IotOverviewVO overview = iotDashboardMapper.selectOverview();
        return overview == null ? new IotOverviewVO() : overview;
    }

    @Override
    public List<SensorRecentDataVO> getRecentData(Integer sensorId, Integer limit) {
        if (sensorId == null) {
            return Collections.emptyList();
        }
        return iotDashboardMapper.selectRecentData(sensorId, limit);
    }

    @Override
    public List<DeviceAlertVO> getLatestAlerts(Integer limit) {
        return iotDashboardMapper.selectLatestAlerts(limit);
    }

    @Override
    public SensorHistoryTrendVO getHistoryTrend(Long sensorId, Integer hours) {
        SensorHistoryTrendVO trend = iotDashboardMapper.selectSensorTrendMeta(sensorId);
        if (trend == null) {
            throw new BusinessException(404, "Sensor not found");
        }
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(hours);
        trend.setStartTime(startTime);
        trend.setEndTime(endTime);
        trend.setPoints(iotDashboardMapper.selectHistoryTrendPoints(sensorId, startTime, endTime));
        return trend;
    }
}
