package com.smartfarm.modules.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.iot.dto.SensorDataSaveRequest;
import com.smartfarm.modules.iot.vo.SensorDataPageVO;

import java.time.LocalDateTime;

public interface SensorDataService {

    IPage<SensorDataPageVO> pageSensorData(long pageNum,
                                           long pageSize,
                                           Long sensorId,
                                           Long plotId,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime);

    void createSensorData(SensorDataSaveRequest request);
}
