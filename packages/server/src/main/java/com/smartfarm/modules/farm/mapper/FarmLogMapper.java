package com.smartfarm.modules.farm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.modules.farm.entity.FarmLog;
import com.smartfarm.modules.farm.vo.FarmLogVO;
import com.smartfarm.modules.farm.vo.LogSummaryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FarmLogMapper extends BaseMapper<FarmLog> {

    @Select("""
            SELECT
                fl.log_id AS logId,
                fl.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                fl.operator_id AS operatorId,
                u.real_name AS operatorName,
                fl.operation_type AS operationType,
                fl.operation_date AS operationDate,
                fl.description,
                fl.create_time AS createTime
            FROM farm_log fl
            LEFT JOIN planting_plan pp ON pp.plan_id = fl.plan_id
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            LEFT JOIN user u ON u.user_id = fl.operator_id
            WHERE (#{planId} IS NULL OR fl.plan_id = #{planId})
              AND (#{operatorId} IS NULL OR fl.operator_id = #{operatorId})
              AND (#{operationType} IS NULL OR #{operationType} = '' OR fl.operation_type = #{operationType})
              AND (#{startDate} IS NULL OR fl.operation_date >= #{startDate})
              AND (#{endDate} IS NULL OR fl.operation_date <= #{endDate})
            ORDER BY fl.operation_date DESC, fl.log_id DESC
            """)
    IPage<FarmLogVO> selectLogPage(Page<FarmLogVO> page,
                                   @Param("planId") Long planId,
                                   @Param("operatorId") Long operatorId,
                                   @Param("operationType") String operationType,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT
                fl.log_id AS logId,
                fl.plan_id AS planId,
                p.plot_name AS plotName,
                c.crop_name AS cropName,
                fl.operator_id AS operatorId,
                u.real_name AS operatorName,
                fl.operation_type AS operationType,
                fl.operation_date AS operationDate,
                fl.description,
                fl.create_time AS createTime
            FROM farm_log fl
            LEFT JOIN planting_plan pp ON pp.plan_id = fl.plan_id
            LEFT JOIN plot p ON p.plot_id = pp.plot_id
            LEFT JOIN crop c ON c.crop_id = pp.crop_id
            LEFT JOIN user u ON u.user_id = fl.operator_id
            WHERE fl.log_id = #{logId}
            """)
    FarmLogVO selectLogById(@Param("logId") Long logId);

    @Select("""
            SELECT operation_type AS operationType, COUNT(*) AS totalNum
            FROM farm_log
            WHERE (#{startDate} IS NULL OR operation_date >= #{startDate})
              AND (#{endDate} IS NULL OR operation_date <= #{endDate})
            GROUP BY operation_type
            ORDER BY totalNum DESC, operation_type ASC
            """)
    List<LogSummaryVO> selectLogSummary(@Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);
}
