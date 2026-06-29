package com.smartfarm.modules.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.iot.entity.SensorData;
import com.smartfarm.modules.iot.vo.SensorDataPageVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface SensorDataMapper extends BaseMapper<SensorData> {

    @Select("""
            SELECT
                sd.data_id AS dataId,
                sd.sensor_id AS sensorId,
                s.sensor_name AS sensorName,
                s.sensor_type AS sensorType,
                s.plot_id AS plotId,
                p.plot_name AS plotName,
                sd.collect_time AS collectTime,
                sd.temperature AS temperature,
                sd.humidity AS humidity,
                sd.soil_moisture AS soilMoisture,
                sd.light_intensity AS lightIntensity
            FROM sensor_data sd
            JOIN sensor s ON s.sensor_id = sd.sensor_id
            LEFT JOIN plot p ON p.plot_id = s.plot_id
            WHERE s.deleted = 0
              AND (#{sensorId} IS NULL OR sd.sensor_id = #{sensorId})
              AND (#{plotId} IS NULL OR s.plot_id = #{plotId})
              AND (#{startTime} IS NULL OR sd.collect_time >= #{startTime})
              AND (#{endTime} IS NULL OR sd.collect_time <= #{endTime})
            ORDER BY sd.collect_time DESC, sd.data_id DESC
            """)
    IPage<SensorDataPageVO> selectPageData(Page<SensorDataPageVO> page,
                                           @Param("sensorId") Long sensorId,
                                           @Param("plotId") Long plotId,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}
