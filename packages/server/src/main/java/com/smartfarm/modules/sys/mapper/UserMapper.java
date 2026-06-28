package com.smartfarm.modules.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfarm.modules.sys.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT COUNT(*) FROM planting_plan WHERE created_by = #{userId}")
    int countPlantingPlanByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM irrigation_record WHERE operator_id = #{userId}")
    int countIrrigationRecordByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM farm_log WHERE operator_id = #{userId}")
    int countFarmLogByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM agri_purchase_record WHERE buyer_id = #{userId}")
    int countAgriPurchaseRecordByUserId(@Param("userId") Long userId);
}
