package com.smartfarm.modules.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.plan.entity.PlantingPlan;
import com.smartfarm.modules.plan.vo.PlanCalendarVO;
import com.smartfarm.modules.plan.vo.PlantingPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PlantingPlanMapper extends BaseMapper<PlantingPlan> {

    @Select("""
            SELECT
                pp.plan_id AS planId,
                pp.plot_id AS plotId,
                p.plot_name AS plotName,
                pp.crop_id AS cropId,
                c.crop_name AS cropName,
                c.category,
                c.growth_cycle_days AS growthCycleDays,
                pp.start_date AS startDate,
                pp.expected_harvest AS expectedHarvest,
                pp.plant_area AS plantArea,
                pp.status,
                pp.created_by AS createdBy,
                u.real_name AS creatorName,
                DATEDIFF(pp.expected_harvest, CURDATE()) AS daysToHarvest,
                CASE
                    WHEN pp.status = '已取消' THEN 0
                    WHEN DATEDIFF(pp.expected_harvest, pp.start_date) <= 0 THEN 100
                    WHEN CURDATE() <= pp.start_date THEN 0
                    WHEN CURDATE() >= pp.expected_harvest THEN 100
                    ELSE ROUND(DATEDIFF(CURDATE(), pp.start_date) * 100 / DATEDIFF(pp.expected_harvest, pp.start_date))
                END AS progressPercent,
                pp.created_at AS createdAt
            FROM planting_plan pp
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            LEFT JOIN user u ON u.user_id = pp.created_by
            WHERE (#{plotId} IS NULL OR pp.plot_id = #{plotId})
              AND (#{cropId} IS NULL OR pp.crop_id = #{cropId})
              AND (#{status} IS NULL OR #{status} = '' OR pp.status = #{status})
              AND (#{keyword} IS NULL OR #{keyword} = '' OR p.plot_name LIKE CONCAT('%', #{keyword}, '%') OR c.crop_name LIKE CONCAT('%', #{keyword}, '%'))
              AND (#{startDate} IS NULL OR pp.start_date >= #{startDate})
              AND (#{endDate} IS NULL OR pp.expected_harvest <= #{endDate})
            ORDER BY FIELD(pp.status, '进行中', '未开始', '已完成', '已取消'), pp.start_date DESC, pp.plan_id DESC
            """)
    IPage<PlantingPlanVO> selectPlanPage(Page<PlantingPlanVO> page,
                                         @Param("plotId") Long plotId,
                                         @Param("cropId") Long cropId,
                                         @Param("status") String status,
                                         @Param("keyword") String keyword,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT
                pp.plan_id AS planId,
                pp.plot_id AS plotId,
                p.plot_name AS plotName,
                pp.crop_id AS cropId,
                c.crop_name AS cropName,
                c.category,
                c.growth_cycle_days AS growthCycleDays,
                pp.start_date AS startDate,
                pp.expected_harvest AS expectedHarvest,
                pp.plant_area AS plantArea,
                pp.status,
                pp.created_by AS createdBy,
                u.real_name AS creatorName,
                DATEDIFF(pp.expected_harvest, CURDATE()) AS daysToHarvest,
                CASE
                    WHEN pp.status = '已取消' THEN 0
                    WHEN DATEDIFF(pp.expected_harvest, pp.start_date) <= 0 THEN 100
                    WHEN CURDATE() <= pp.start_date THEN 0
                    WHEN CURDATE() >= pp.expected_harvest THEN 100
                    ELSE ROUND(DATEDIFF(CURDATE(), pp.start_date) * 100 / DATEDIFF(pp.expected_harvest, pp.start_date))
                END AS progressPercent,
                pp.created_at AS createdAt
            FROM planting_plan pp
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            LEFT JOIN user u ON u.user_id = pp.created_by
            WHERE pp.plan_id = #{planId}
            """)
    PlantingPlanVO selectPlanById(@Param("planId") Long planId);

    @Select("""
            SELECT
                pp.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                pp.status,
                pp.start_date AS startDate,
                pp.expected_harvest AS expectedHarvest,
                DATEDIFF(pp.expected_harvest, CURDATE()) AS daysToHarvest
            FROM planting_plan pp
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            WHERE pp.start_date <= #{endDate}
              AND pp.expected_harvest >= #{startDate}
            ORDER BY pp.start_date ASC, pp.expected_harvest ASC
            """)
    List<PlanCalendarVO> selectCalendarPlans(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Select("SELECT COUNT(*) FROM planting_plan WHERE status = '进行中'")
    long countOngoing();

    @Select("SELECT COUNT(*) FROM planting_plan WHERE expected_harvest BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) AND status IN ('未开始', '进行中')")
    long countUpcomingHarvest();

    @Select("SELECT COUNT(*) FROM planting_plan WHERE start_date = CURDATE() AND status <> '已取消'")
    long countStartToday();

    @Select("SELECT COUNT(*) FROM planting_plan WHERE expected_harvest < CURDATE() AND status IN ('未开始', '进行中')")
    long countOverdue();

    @Select("SELECT * FROM planting_plan WHERE status <> '已取消'")
    List<PlantingPlan> selectLifecyclePlans();
}
