package com.smartfarm.modules.iot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.iot.dto.SensorSaveRequest;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.mapper.SensorManageMapper;
import com.smartfarm.modules.iot.service.SensorManageService;
import com.smartfarm.modules.iot.vo.SensorOnlineStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SensorManageServiceImpl implements SensorManageService {

    private final SensorManageMapper sensorManageMapper;

    @Override
    public IPage<Sensor> pageSensors(long pageNum, long pageSize, Long plotId, String status, String sensorType, String keyword) {
        LambdaQueryWrapper<Sensor> wrapper = new LambdaQueryWrapper<Sensor>()
                .eq(plotId != null, Sensor::getPlotId, plotId)
                .eq(StringUtils.hasText(status), Sensor::getStatus, status)
                .eq(StringUtils.hasText(sensorType), Sensor::getSensorType, sensorType)
                .and(StringUtils.hasText(keyword), q -> q
                        .like(Sensor::getSensorName, keyword)
                        .or()
                        .like(Sensor::getSensorType, keyword))
                .orderByDesc(Sensor::getSensorId);
        return sensorManageMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Sensor getSensorById(Long sensorId) {
        Sensor sensor = sensorManageMapper.selectById(sensorId);
        if (sensor == null) {
            throw new BusinessException(404, "Sensor not found");
        }
        return sensor;
    }

    @Override
    public void createSensor(SensorSaveRequest request) {
        checkDuplicateName(null, request.getSensorName());
        Sensor sensor = new Sensor();
        BeanUtils.copyProperties(request, sensor);
        sensorManageMapper.insert(sensor);
    }

    @Override
    public void updateSensor(Long sensorId, SensorSaveRequest request) {
        Sensor sensor = getSensorById(sensorId);
        checkDuplicateName(sensorId, request.getSensorName());
        BeanUtils.copyProperties(request, sensor);
        sensorManageMapper.updateById(sensor);
    }

    @Override
    public void deleteSensor(Long sensorId) {
        getSensorById(sensorId);
        sensorManageMapper.deleteById(sensorId);
    }

    @Override
    public IPage<SensorOnlineStatusVO> pageOnlineStatus(long pageNum, long pageSize, String status) {
        return sensorManageMapper.selectOnlineStatus(new Page<>(pageNum, pageSize), status);
    }

    private void checkDuplicateName(Long sensorId, String sensorName) {
        Sensor sensor = sensorManageMapper.selectOne(new LambdaQueryWrapper<Sensor>()
                .eq(Sensor::getSensorName, sensorName)
                .ne(sensorId != null, Sensor::getSensorId, sensorId)
                .last("LIMIT 1"));
        if (sensor != null) {
            throw new BusinessException("Sensor name already exists");
        }
    }
}
