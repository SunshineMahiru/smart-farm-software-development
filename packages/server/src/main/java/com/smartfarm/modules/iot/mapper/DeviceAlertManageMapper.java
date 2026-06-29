package com.smartfarm.modules.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.iot.entity.DeviceAlert;
import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface DeviceAlertManageMapper extends BaseMapper<DeviceAlert> {

    @Select("""
            SELECT
                da.alert_id AS alertId,
                da.plot_id AS plotId,
                p.plot_name AS plotName,
                da.alert_time AS alertTime,
                da.alert_type AS alertType,
                da.alert_value AS alertValue,
                da.status AS status
            FROM device_alert da
            LEFT JOIN plot p ON p.plot_id = da.plot_id
            WHERE (#{plotId} IS NULL OR da.plot_id = #{plotId})
              AND (#{status} IS NULL OR #{status} = '' OR da.status = #{status})
              AND (#{startTime} IS NULL OR da.alert_time >= #{startTime})
              AND (#{endTime} IS NULL OR da.alert_time <= #{endTime})
              AND (
                    #{keyword} IS NULL OR #{keyword} = ''
                    OR da.alert_type LIKE CONCAT('%', #{keyword}, '%')
                    OR p.plot_name LIKE CONCAT('%', #{keyword}, '%')
                  )
            ORDER BY da.alert_time DESC, da.alert_id DESC
            """)
    IPage<DeviceAlertVO> selectPageAlerts(Page<DeviceAlertVO> page,
                                          @Param("plotId") Long plotId,
                                          @Param("status") String status,
                                          @Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("keyword") String keyword);

    @Select("""
            SELECT
                da.alert_id AS alertId,
                da.plot_id AS plotId,
                p.plot_name AS plotName,
                da.alert_time AS alertTime,
                da.alert_type AS alertType,
                da.alert_value AS alertValue,
                da.status AS status
            FROM device_alert da
            LEFT JOIN plot p ON p.plot_id = da.plot_id
            WHERE da.alert_id = #{alertId}
            LIMIT 1
            """)
    DeviceAlertVO selectAlertDetail(@Param("alertId") Long alertId);
}
