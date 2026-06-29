package com.smartfarm.modules.iot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;

import java.time.LocalDateTime;

public interface DeviceAlertService {

    IPage<DeviceAlertVO> pageAlerts(long pageNum,
                                    long pageSize,
                                    Long plotId,
                                    String status,
                                    LocalDateTime startTime,
                                    LocalDateTime endTime,
                                    String keyword);

    DeviceAlertVO getAlertById(Long alertId);

    void updateAlertStatus(Long alertId, String status);

    void deleteAlert(Long alertId);
}
