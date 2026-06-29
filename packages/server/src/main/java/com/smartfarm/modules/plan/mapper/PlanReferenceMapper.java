package com.smartfarm.modules.plan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface PlanReferenceMapper {

    @Select("SELECT COUNT(*) FROM plot WHERE plot_id = #{plotId}")
    int countPlotById(@Param("plotId") Long plotId);

    @Select("SELECT COUNT(*) FROM crop WHERE crop_id = #{cropId}")
    int countCropById(@Param("cropId") Long cropId);

    @Select("SELECT COUNT(*) FROM user WHERE user_id = #{userId}")
    int countUserById(@Param("userId") Long userId);

    @Select("SELECT status FROM plot WHERE plot_id = #{plotId}")
    String selectPlotStatus(@Param("plotId") Long plotId);

    @Select("SELECT area FROM plot WHERE plot_id = #{plotId}")
    BigDecimal selectPlotArea(@Param("plotId") Long plotId);

    @Select("SELECT growth_cycle_days FROM crop WHERE crop_id = #{cropId}")
    Integer selectGrowthCycleDays(@Param("cropId") Long cropId);

    @Select("""
            SELECT COUNT(*)
            FROM planting_plan
            WHERE plot_id = #{plotId}
              AND status <> '已取消'
              AND plan_id <> COALESCE(#{excludePlanId}, -1)
              AND start_date <= #{expectedHarvest}
              AND expected_harvest >= #{startDate}
            """)
    int countOverlappingPlans(@Param("plotId") Long plotId,
                              @Param("startDate") java.time.LocalDate startDate,
                              @Param("expectedHarvest") java.time.LocalDate expectedHarvest,
                              @Param("excludePlanId") Long excludePlanId);

    @Select("""
            SELECT COUNT(*)
            FROM planting_plan
            WHERE plot_id = #{plotId}
              AND status IN ('未开始', '进行中')
            """)
    int countActivePlanByPlotId(@Param("plotId") Long plotId);

    @Update("UPDATE plot SET status = #{status} WHERE plot_id = #{plotId}")
    int updatePlotStatus(@Param("plotId") Long plotId, @Param("status") String status);
}
