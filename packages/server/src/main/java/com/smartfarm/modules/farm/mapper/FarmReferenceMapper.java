package com.smartfarm.modules.farm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FarmReferenceMapper {

    @Select("SELECT COUNT(*) FROM planting_plan WHERE plan_id = #{planId}")
    int countPlanById(@Param("planId") Long planId);

    @Select("SELECT COUNT(*) FROM user WHERE user_id = #{userId}")
    int countUserById(@Param("userId") Long userId);
}
