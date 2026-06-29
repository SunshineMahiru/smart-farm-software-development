package com.smartfarm.modules.iot.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.iot.entity.DeviceAlert;
import com.smartfarm.modules.iot.mapper.DeviceAlertManageMapper;
import com.smartfarm.modules.iot.service.DeviceAlertService;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceAlertServiceImpl implements DeviceAlertService {

    private final DeviceAlertManageMapper deviceAlertManageMapper;

    @Override
    public IPage<DeviceAlertVO> pageAlerts(long pageNum,
                                           long pageSize,
                                           Long plotId,
                                           String status,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime,
                                           String keyword) {
        validateTimeRange(startTime, endTime);
        return deviceAlertManageMapper.selectPageAlerts(new Page<>(pageNum, pageSize), plotId, status, startTime, endTime, keyword);
    }

    @Override
    public DeviceAlertVO getAlertById(Long alertId) {
        DeviceAlertVO alert = deviceAlertManageMapper.selectAlertDetail(alertId);
        if (alert == null) {
            throw new BusinessException(404, "Device alert not found");
        }
        return alert;
    }

    @Override
    public void updateAlertStatus(Long alertId, String status) {
        DeviceAlert alert = getEntityById(alertId);
        alert.setStatus(status);
        deviceAlertManageMapper.updateById(alert);
    }

    @Override
    public void deleteAlert(Long alertId) {
        getEntityById(alertId);
        deviceAlertManageMapper.deleteById(alertId);
    }

    private DeviceAlert getEntityById(Long alertId) {
        DeviceAlert alert = deviceAlertManageMapper.selectById(alertId);
        if (alert == null) {
            throw new BusinessException(404, "Device alert not found");
        }
        return alert;
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException("Start time cannot be after end time");
        }
    }
}
