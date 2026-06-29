package com.smartfarm.modules.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.iot.entity.Sensor;
import com.smartfarm.modules.iot.vo.SensorOnlineStatusVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SensorManageMapper extends BaseMapper<Sensor> {

    @Select("""
            SELECT
                s.sensor_id AS sensorId,
                s.plot_id AS plotId,
                p.plot_name AS plotName,
                s.sensor_name AS sensorName,
                s.sensor_type AS sensorType,
                s.status AS status,
                sd.collect_time AS latestCollectTime,
                sd.temperature AS latestTemperature,
                sd.humidity AS latestHumidity
            FROM sensor s
            LEFT JOIN plot p ON p.plot_id = s.plot_id
            LEFT JOIN sensor_data sd ON sd.sensor_id = s.sensor_id
                AND sd.collect_time = (
                    SELECT MAX(d.collect_time)
                    FROM sensor_data d
                    WHERE d.sensor_id = s.sensor_id
                )
            WHERE (#{status} IS NULL OR #{status} = '' OR s.status = #{status})
            ORDER BY s.sensor_id DESC
            """)
    IPage<SensorOnlineStatusVO> selectOnlineStatus(Page<SensorOnlineStatusVO> page, @Param("status") String status);
}
