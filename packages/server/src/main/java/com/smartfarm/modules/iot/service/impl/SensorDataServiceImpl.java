package com.smartfarm.modules.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.common.websocket.WebSocketServer;
import com.smartfarm.modules.iot.dto.SensorDataSaveRequest;
import com.smartfarm.modules.iot.entity.DeviceAlert;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.entity.SensorData;
import com.smartfarm.modules.iot.mapper.DeviceAlertManageMapper;
import com.smartfarm.modules.iot.mapper.SensorDataMapper;
import com.smartfarm.modules.iot.mapper.SensorManageMapper;
import com.smartfarm.modules.iot.service.SensorDataService;
import com.smartfarm.modules.iot.vo.SensorDataPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    private static final BigDecimal HIGH_TEMPERATURE_THRESHOLD = new BigDecimal("35");
    private static final BigDecimal LOW_TEMPERATURE_THRESHOLD = new BigDecimal("5");
    private static final BigDecimal LOW_HUMIDITY_THRESHOLD = new BigDecimal("30");
    private static final BigDecimal HIGH_HUMIDITY_THRESHOLD = new BigDecimal("90");
    private static final BigDecimal LOW_SOIL_MOISTURE_THRESHOLD = new BigDecimal("20");
    private static final BigDecimal HIGH_LIGHT_INTENSITY_THRESHOLD = new BigDecimal("5500");

    private final SensorDataMapper sensorDataMapper;
    private final SensorManageMapper sensorManageMapper;
    private final DeviceAlertManageMapper deviceAlertManageMapper;
    private final WebSocketServer webSocketServer;

    @Override
    public IPage<SensorDataPageVO> pageSensorData(long pageNum,
                                                  long pageSize,
                                                  Long sensorId,
                                                  Long plotId,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime) {
        validateTimeRange(startTime, endTime);
        return sensorDataMapper.selectPageData(new Page<>(pageNum, pageSize), sensorId, plotId, startTime, endTime);
    }

    @Override
    public void createSensorData(SensorDataSaveRequest request) {
        validateMeasurementPresent(request);
        Sensor sensor = getSensor(request.getSensorId());

        SensorData sensorData = new SensorData();
        sensorData.setSensorId(request.getSensorId());
        sensorData.setCollectTime(request.getCollectTime() == null ? LocalDateTime.now() : request.getCollectTime());
        sensorData.setTemperature(request.getTemperature());
        sensorData.setHumidity(request.getHumidity());
        sensorData.setSoilMoisture(request.getSoilMoisture());
        sensorData.setLightIntensity(request.getLightIntensity());
        sensorDataMapper.insert(sensorData);

        if (!"在线".equals(sensor.getStatus())) {
            sensor.setStatus("在线");
            sensorManageMapper.updateById(sensor);
        }

        List<String> alertMessages = createAlertsIfNeeded(sensor, sensorData);
        for (String alertMessage : alertMessages) {
            webSocketServer.broadcastAll(alertMessage);
        }
    }

    private Sensor getSensor(Long sensorId) {
        Sensor sensor = sensorManageMapper.selectById(sensorId);
        if (sensor == null || Integer.valueOf(1).equals(sensor.getDeleted())) {
            throw new BusinessException(404, "Sensor not found");
        }
        return sensor;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("Start time cannot be after end time");
        }
    }

    private void validateMeasurementPresent(SensorDataSaveRequest request) {
        if (request.getTemperature() == null
                && request.getHumidity() == null
                && request.getSoilMoisture() == null
                && request.getLightIntensity() == null) {
            throw new BusinessException("At least one sensor measurement must be provided");
        }
    }

    private List<String> createAlertsIfNeeded(Sensor sensor, SensorData sensorData) {
        List<String> messages = new ArrayList<>();
        appendAlert(messages, sensor, sensorData.getCollectTime(), "区域空气温度过高", sensorData.getTemperature(), HIGH_TEMPERATURE_THRESHOLD, true);
        appendAlert(messages, sensor, sensorData.getCollectTime(), "区域空气温度过低", sensorData.getTemperature(), LOW_TEMPERATURE_THRESHOLD, false);
        appendAlert(messages, sensor, sensorData.getCollectTime(), "区域空气湿度过低", sensorData.getHumidity(), LOW_HUMIDITY_THRESHOLD, false);
        appendAlert(messages, sensor, sensorData.getCollectTime(), "区域空气湿度过高", sensorData.getHumidity(), HIGH_HUMIDITY_THRESHOLD, true);
        appendAlert(messages, sensor, sensorData.getCollectTime(), "浅层土壤湿度过低", sensorData.getSoilMoisture(), LOW_SOIL_MOISTURE_THRESHOLD, false);
        appendAlert(messages, sensor, sensorData.getCollectTime(), "极端强光日照警告", sensorData.getLightIntensity(), HIGH_LIGHT_INTENSITY_THRESHOLD, true);
        return messages;
    }

    private void appendAlert(List<String> messages,
                             Sensor sensor,
                             LocalDateTime alertTime,
                             String alertType,
                             BigDecimal actualValue,
                             BigDecimal threshold,
                             boolean greaterThan) {
        if (actualValue == null) {
            return;
        }
        boolean matched = greaterThan ? actualValue.compareTo(threshold) > 0 : actualValue.compareTo(threshold) < 0;
        if (!matched) {
            return;
        }

        DeviceAlert alert = new DeviceAlert();
        alert.setPlotId(sensor.getPlotId());
        alert.setAlertTime(alertTime);
        alert.setAlertType(alertType);
        alert.setAlertValue(actualValue);
        alert.setStatus("未处理");
        deviceAlertManageMapper.insert(alert);
        messages.add("【IoT告警】" + sensor.getSensorName() + " 出现" + alertType + "，当前值：" + actualValue.stripTrailingZeros().toPlainString());
    }
}
