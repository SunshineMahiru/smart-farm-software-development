package com.smartfarm.modules.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartfarm.modules.plan.entity.Crop;
import com.smartfarm.modules.plan.vo.CropOptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CropMapper extends BaseMapper<Crop> {

    @Select("""
            SELECT crop_id AS cropId,
                   crop_name AS cropName,
                   category,
                   growth_cycle_days AS growthCycleDays
            FROM crop
            ORDER BY category ASC, crop_name ASC
            """)
    List<CropOptionVO> selectOptions();

    @Select("SELECT COUNT(*) FROM planting_plan WHERE crop_id = #{cropId}")
    int countPlanByCropId(Long cropId);
}
