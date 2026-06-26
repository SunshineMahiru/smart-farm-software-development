package com.smartfarm.modules.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.iot.dto.SensorSaveRequest;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.vo.SensorOnlineStatusVO;

public interface SensorManageService {

    IPage<Sensor> pageSensors(long pageNum, long pageSize, Long plotId, String status, String sensorType, String keyword);

    Sensor getSensorById(Long sensorId);

    void createSensor(SensorSaveRequest request);

    void updateSensor(Long sensorId, SensorSaveRequest request);

    void deleteSensor(Long sensorId);

    IPage<SensorOnlineStatusVO> pageOnlineStatus(long pageNum, long pageSize, String status);
}
