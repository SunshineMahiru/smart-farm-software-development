package com.smartfarm.modules.iot.service.impl;

import com.smartfarm.modules.iot.mapper.IotDashboardMapper;
import com.smartfarm.modules.iot.service.IotDashboardService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
