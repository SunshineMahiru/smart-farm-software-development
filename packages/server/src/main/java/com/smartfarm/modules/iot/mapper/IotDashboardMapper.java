package com.smartfarm.modules.iot.mapper;

import com.smartfarm.modules.iot.vo.DeviceAlertVO;
import com.smartfarm.modules.iot.vo.IotOverviewVO;
import com.smartfarm.modules.iot.vo.SensorHistoryPointVO;
import com.smartfarm.modules.iot.vo.SensorHistoryTrendVO;
import com.smartfarm.modules.iot.vo.SensorRecentDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface IotDashboardMapper {

    @Select("""
            SELECT
                (SELECT COUNT(*) FROM sensor) AS totalSensors,
                (SELECT COUNT(*) FROM sensor WHERE status = _utf8mb4 0xE59CA8E7BABF) AS onlineSensors,
                (SELECT COUNT(*) FROM sensor WHERE status = _utf8mb4 0xE7A6BBE7BABF) AS offlineSensors,
                (SELECT COUNT(*) FROM sensor_data WHERE DATE(collect_time) = CURDATE()) AS todayDataCount,
                (SELECT COUNT(*) FROM device_alert) AS totalAlerts,
                (SELECT COUNT(*) FROM device_alert WHERE status = _utf8mb4 0xE69CAAE5A484E79086) AS pendingAlerts,
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

    @Select("""
            SELECT
                s.sensor_id AS sensorId,
                s.sensor_name AS sensorName,
                s.sensor_type AS sensorType
            FROM sensor s
            WHERE s.sensor_id = #{sensorId}
            LIMIT 1
            """)
    SensorHistoryTrendVO selectSensorTrendMeta(@Param("sensorId") Long sensorId);

    @Select("""
            SELECT
                collect_time AS collectTime,
                temperature,
                humidity
            FROM sensor_data
            WHERE sensor_id = #{sensorId}
              AND collect_time >= #{startTime}
              AND collect_time <= #{endTime}
            ORDER BY collect_time ASC
            """)
    List<SensorHistoryPointVO> selectHistoryTrendPoints(
            @Param("sensorId") Long sensorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
