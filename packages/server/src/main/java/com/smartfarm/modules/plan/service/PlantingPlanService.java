package com.smartfarm.modules.plan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.plan.dto.PlantingPlanSaveRequest;
import com.smartfarm.modules.plan.vo.PlanCalendarVO;
import com.smartfarm.modules.plan.vo.PlanReminderVO;
import com.smartfarm.modules.plan.vo.PlantingPlanVO;

import java.time.LocalDate;
import java.util.List;

public interface PlantingPlanService {

    IPage<PlantingPlanVO> pagePlans(long pageNum, long pageSize, Long plotId, Long cropId, String status,
                                    String keyword, LocalDate startDate, LocalDate endDate);

    PlantingPlanVO getPlanById(Long planId);

    List<PlanCalendarVO> listCalendarPlans(LocalDate startDate, LocalDate endDate);

    PlanReminderVO getReminderSummary();

    void createPlan(PlantingPlanSaveRequest request);

    void updatePlan(Long planId, PlantingPlanSaveRequest request);

    void deletePlan(Long planId);

    void refreshLifecycleAndBroadcast();
}
