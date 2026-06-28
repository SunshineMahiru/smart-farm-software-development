package com.smartfarm.modules.farm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.farm.dto.YieldStatSaveRequest;
import com.smartfarm.modules.farm.vo.PlanYieldSummaryVO;
import com.smartfarm.modules.farm.vo.YieldStatVO;

import java.time.LocalDate;
import java.util.List;

public interface YieldStatService {

    IPage<YieldStatVO> pageYieldStats(long pageNum, long pageSize, Long planId, String qualityGrade,
                                      LocalDate startDate, LocalDate endDate);

    YieldStatVO getYieldById(Long yieldId);

    void createYield(YieldStatSaveRequest request);

    void updateYield(Long yieldId, YieldStatSaveRequest request);

    void deleteYield(Long yieldId);

    List<PlanYieldSummaryVO> getPlanYieldSummary(Long planId);
}
