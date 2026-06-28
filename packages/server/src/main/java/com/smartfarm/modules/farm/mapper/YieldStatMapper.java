package com.smartfarm.modules.farm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.farm.entity.YieldStat;
import com.smartfarm.modules.farm.vo.PlanYieldSummaryVO;
import com.smartfarm.modules.farm.vo.YieldStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface YieldStatMapper extends BaseMapper<YieldStat> {

    @Select("""
            SELECT
                ys.yield_id AS yieldId,
                ys.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                ys.harvest_date AS harvestDate,
                ys.yield_weight AS yieldWeight,
                ys.quality_grade AS qualityGrade,
                ys.create_time AS createTime
            FROM yield_stat ys
            LEFT JOIN planting_plan pp ON pp.plan_id = ys.plan_id
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            WHERE (#{planId} IS NULL OR ys.plan_id = #{planId})
              AND (#{qualityGrade} IS NULL OR #{qualityGrade} = '' OR ys.quality_grade = #{qualityGrade})
              AND (#{startDate} IS NULL OR ys.harvest_date >= #{startDate})
              AND (#{endDate} IS NULL OR ys.harvest_date <= #{endDate})
            ORDER BY ys.harvest_date DESC, ys.yield_id DESC
            """)
    IPage<YieldStatVO> selectYieldPage(Page<YieldStatVO> page,
                                       @Param("planId") Long planId,
                                       @Param("qualityGrade") String qualityGrade,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT
                ys.yield_id AS yieldId,
                ys.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                ys.harvest_date AS harvestDate,
                ys.yield_weight AS yieldWeight,
                ys.quality_grade AS qualityGrade,
                ys.create_time AS createTime
            FROM yield_stat ys
            LEFT JOIN planting_plan pp ON pp.plan_id = ys.plan_id
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            WHERE ys.yield_id = #{yieldId}
            """)
    YieldStatVO selectYieldById(@Param("yieldId") Long yieldId);

    @Select("""
            SELECT
                ys.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                COUNT(*) AS recordCount,
                SUM(ys.yield_weight) AS totalYield,
                AVG(ys.yield_weight) AS avgYield,
                MAX(ys.harvest_date) AS latestHarvestDate
            FROM yield_stat ys
            LEFT JOIN planting_plan pp ON pp.plan_id = ys.plan_id
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            WHERE (#{planId} IS NULL OR ys.plan_id = #{planId})
            GROUP BY ys.plan_id, p.plot_name, c.crop_name
            ORDER BY latestHarvestDate DESC, ys.plan_id DESC
            """)
    List<PlanYieldSummaryVO> selectPlanYieldSummary(@Param("planId") Long planId);
}
