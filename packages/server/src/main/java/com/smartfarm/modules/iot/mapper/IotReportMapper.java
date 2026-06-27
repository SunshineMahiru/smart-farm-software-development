package com.smartfarm.modules.iot.mapper;

import com.smartfarm.modules.iot.vo.IotDailyAggregateVO;
import com.smartfarm.modules.iot.vo.IotDailyReportVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Mapper
public interface IotReportMapper {

    @Select("""
            SELECT
                ROUND(AVG(sd.temperature), 2) AS avgTemperature,
                MIN(sd.temperature) AS minTemperature,
                MAX(sd.temperature) AS maxTemperature,
                ROUND(AVG(sd.humidity), 2) AS avgHumidity,
                MIN(sd.humidity) AS minHumidity,
                MAX(sd.humidity) AS maxHumidity,
                ROUND(AVG(sd.soil_moisture), 2) AS avgSoilMoisture,
                ROUND(AVG(sd.light_intensity), 2) AS avgLightIntensity,
                (
                    SELECT COUNT(*)
                    FROM device_alert da
                    WHERE da.alert_time >= #{startTime} AND da.alert_time < #{endTime}
                ) AS totalAlerts,
                (
                    SELECT COUNT(*)
                    FROM device_alert da
                    WHERE da.alert_time >= #{startTime} AND da.alert_time < #{endTime}
                      AND da.status = '未处理'
                ) AS pendingAlerts
            FROM sensor_data sd
            WHERE sd.collect_time >= #{startTime} AND sd.collect_time < #{endTime}
            """)
    IotDailyAggregateVO selectAggregate(@Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    @Select("""
            SELECT
                report_id AS reportId,
                report_date AS reportDate,
                generated_at AS generatedAt,
                data_start_time AS dataStartTime,
                data_end_time AS dataEndTime,
                avg_temperature AS avgTemperature,
                min_temperature AS minTemperature,
                max_temperature AS maxTemperature,
                avg_humidity AS avgHumidity,
                min_humidity AS minHumidity,
                max_humidity AS maxHumidity,
                avg_soil_moisture AS avgSoilMoisture,
                avg_light_intensity AS avgLightIntensity,
                total_alerts AS totalAlerts,
                pending_alerts AS pendingAlerts,
                report_content AS reportContent
            FROM iot_daily_report
            WHERE report_date = #{reportDate}
            LIMIT 1
            """)
    IotDailyReportVO selectByReportDate(@Param("reportDate") LocalDate reportDate);

    @Select("""
            SELECT
                report_id AS reportId,
                report_date AS reportDate,
                generated_at AS generatedAt,
                data_start_time AS dataStartTime,
                data_end_time AS dataEndTime,
                avg_temperature AS avgTemperature,
                min_temperature AS minTemperature,
                max_temperature AS maxTemperature,
                avg_humidity AS avgHumidity,
                min_humidity AS minHumidity,
                max_humidity AS maxHumidity,
                avg_soil_moisture AS avgSoilMoisture,
                avg_light_intensity AS avgLightIntensity,
                total_alerts AS totalAlerts,
                pending_alerts AS pendingAlerts,
                report_content AS reportContent
            FROM iot_daily_report
            ORDER BY generated_at DESC, report_id DESC
            LIMIT 1
            """)
    IotDailyReportVO selectLatestReport();

    @Insert("""
            INSERT INTO iot_daily_report (
                report_date,
                generated_at,
                data_start_time,
                data_end_time,
                avg_temperature,
                min_temperature,
                max_temperature,
                avg_humidity,
                min_humidity,
                max_humidity,
                avg_soil_moisture,
                avg_light_intensity,
                total_alerts,
                pending_alerts,
                report_content
            ) VALUES (
                #{reportDate},
                #{generatedAt},
                #{dataStartTime},
                #{dataEndTime},
                #{avgTemperature},
                #{minTemperature},
                #{maxTemperature},
                #{avgHumidity},
                #{minHumidity},
                #{maxHumidity},
                #{avgSoilMoisture},
                #{avgLightIntensity},
                #{totalAlerts},
                #{pendingAlerts},
                #{reportContent}
            )
            """)
    @Options(useGeneratedKeys = true, keyProperty = "reportId")
    int insert(IotDailyReportVO report);
}
