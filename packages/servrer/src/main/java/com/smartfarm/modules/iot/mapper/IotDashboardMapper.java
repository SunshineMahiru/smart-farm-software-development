package com.smartfarm.modules.iot.mapper;

import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IotDashboardMapper {

    @Select("""
            SELECT
                (SELECT COUNT(*) FROM sensor WHERE deleted = 0) AS totalSensors,
                (SELECT COUNT(*) FROM sensor WHERE deleted = 0 AND status = '鍦ㄧ嚎') AS onlineSensors,
                (SELECT COUNT(*) FROM sensor WHERE deleted = 0 AND status = '绂荤嚎') AS offlineSensors,
                (SELECT COUNT(*) FROM sensor_data WHERE DATE(collect_time) = CURDATE()) AS todayDataCount,
                (SELECT COUNT(*) FROM device_alert) AS totalAlerts,
                (SELECT COUNT(*) FROM device_alert WHERE status = '鏈鐞?) AS pendingAlerts,
                (SELECT MAX(collect_time) FROM sensor_data) AS latestCollectTime
            """)
    IotOverviewVO selectOverview();

    @Select("""
            SELECT
                data_id AS dataId,
                sensor_id AS sensorId,
                collect_time AS collectTime,
                temperature,
                humidity,
                soil_moisture AS soilMoisture,
                light_intensity AS lightIntensity
            FROM sensor_data
            WHERE sensor_id = #{sensorId}
            ORDER BY collect_time DESC
            LIMIT #{limit}
            """)
    List<SensorRecentDataVO> selectRecentData(@Param("sensorId") Integer sensorId, @Param("limit") Integer limit);

    @Select("""
            SELECT
                da.alert_id AS alertId,
                da.plot_id AS plotId,
                p.plot_name AS plotName,
                da.alert_time AS alertTime,
                da.alert_type AS alertType,
                da.alert_value AS alertValue,
                da.status
            FROM device_alert da
            LEFT JOIN plot p ON p.plot_id = da.plot_id
            ORDER BY da.alert_time DESC, da.alert_id DESC
            LIMIT #{limit}
            """)
    List<DeviceAlertVO> selectLatestAlerts(@Param("limit") Integer limit);
}
