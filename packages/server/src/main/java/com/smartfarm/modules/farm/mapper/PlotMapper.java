package com.smartfarm.modules.farm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfarm.modules.farm.entity.Plot;
import com.smartfarm.modules.farm.vo.PlotStatusSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PlotMapper extends BaseMapper<Plot> {

    @Select("""
            SELECT status,
                   COUNT(*) AS totalCount,
                   COALESCE(SUM(area), 0) AS totalArea
            FROM plot
            GROUP BY status
            ORDER BY FIELD(status, '空闲', '种植中', '休耕', '维护中')
            """)
    List<PlotStatusSummaryVO> selectStatusSummary();

    @Select("SELECT COUNT(*) FROM planting_plan WHERE plot_id = #{plotId}")
    int countPlantingPlanByPlotId(@Param("plotId") Long plotId);

    @Select("SELECT COUNT(*) FROM irrigation_record WHERE plot_id = #{plotId}")
    int countIrrigationRecordByPlotId(@Param("plotId") Long plotId);

    @Select("SELECT COUNT(*) FROM sensor WHERE plot_id = #{plotId}")
    int countSensorByPlotId(@Param("plotId") Long plotId);

    @Select("SELECT COUNT(*) FROM device_alert WHERE plot_id = #{plotId}")
    int countDeviceAlertByPlotId(@Param("plotId") Long plotId);
}
