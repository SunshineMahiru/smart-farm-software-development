package com.smartfarm.modules.plan.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.common.websocket.WebSocketServer;
import com.smartfarm.modules.plan.dto.PlantingPlanSaveRequest;
import com.smartfarm.modules.plan.entity.PlantingPlan;
import com.smartfarm.modules.plan.mapper.PlanReferenceMapper;
import com.smartfarm.modules.plan.mapper.PlantingPlanMapper;
import com.smartfarm.modules.plan.service.PlantingPlanService;
import com.smartfarm.modules.plan.vo.PlanCalendarVO;
import com.smartfarm.modules.plan.vo.PlanReminderVO;
import com.smartfarm.modules.plan.vo.PlantingPlanVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantingPlanServiceImpl implements PlantingPlanService {

    private static final String STATUS_NOT_STARTED = "未开始";
    private static final String STATUS_ONGOING = "进行中";
    private static final String STATUS_COMPLETED = "已完成";
    private static final String STATUS_CANCELLED = "已取消";
    private static final String PLOT_STATUS_FALLOW = "休耕";
    private static final String PLOT_STATUS_MAINTAINING = "维护中";
    private static final String PLOT_STATUS_PLANTING = "种植中";
    private static final String PLOT_STATUS_IDLE = "空闲";

    private final PlantingPlanMapper plantingPlanMapper;
    private final PlanReferenceMapper planReferenceMapper;
    private final WebSocketServer webSocketServer;

    @Override
    public IPage<PlantingPlanVO> pagePlans(long pageNum, long pageSize, Long plotId, Long cropId, String status,
                                           String keyword, LocalDate startDate, LocalDate endDate) {
        checkDateRange(startDate, endDate);
        return plantingPlanMapper.selectPlanPage(new Page<>(pageNum, pageSize), plotId, cropId, status, keyword, startDate, endDate);
    }

    @Override
    public PlantingPlanVO getPlanById(Long planId) {
        PlantingPlanVO plan = plantingPlanMapper.selectPlanById(planId);
        if (plan == null) {
            throw new BusinessException(404, "Planting plan not found");
        }
        return plan;
    }

    @Override
    public List<PlanCalendarVO> listCalendarPlans(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("Calendar start date and end date are required");
        }
        checkDateRange(startDate, endDate);
        return plantingPlanMapper.selectCalendarPlans(startDate, endDate);
    }

    @Override
    public PlanReminderVO getReminderSummary() {
        return new PlanReminderVO(
                plantingPlanMapper.countOngoing(),
                plantingPlanMapper.countUpcomingHarvest(),
                plantingPlanMapper.countStartToday(),
                plantingPlanMapper.countOverdue()
        );
    }

    @Override
    @Transactional
    public void createPlan(PlantingPlanSaveRequest request) {
        PlantingPlan plan = buildPlanEntity(null, request);
        plantingPlanMapper.insert(plan);
        refreshPlotStatus(plan.getPlotId());
    }

    @Override
    @Transactional
    public void updatePlan(Long planId, PlantingPlanSaveRequest request) {
        PlantingPlan existing = getPlanEntity(planId);
        Long oldPlotId = existing.getPlotId();
        PlantingPlan plan = buildPlanEntity(planId, request);
        plan.setPlanId(planId);
        plan.setCreatedAt(existing.getCreatedAt());
        plan.setUpdatedAt(existing.getUpdatedAt());
        plantingPlanMapper.updateById(plan);
        refreshPlotStatus(oldPlotId);
        if (!oldPlotId.equals(plan.getPlotId())) {
            refreshPlotStatus(plan.getPlotId());
        }
    }

    @Override
    @Transactional
    public void deletePlan(Long planId) {
        PlantingPlan existing = getPlanEntity(planId);
        plantingPlanMapper.deleteById(planId);
        refreshPlotStatus(existing.getPlotId());
    }

    @Override
    @Transactional
    public void refreshLifecycleAndBroadcast() {
        List<PlantingPlan> plans = plantingPlanMapper.selectLifecyclePlans();
        boolean changed = false;
        for (PlantingPlan plan : plans) {
            String nextStatus = resolvePlanStatus(plan.getStartDate(), plan.getExpectedHarvest(), plan.getStatus());
            if (!nextStatus.equals(plan.getStatus())) {
                plan.setStatus(nextStatus);
                plantingPlanMapper.updateById(plan);
                changed = true;
            }
            refreshPlotStatus(plan.getPlotId());
        }

        PlanReminderVO summary = getReminderSummary();
        if (changed || summary.getUpcomingHarvestCount() > 0 || summary.getStartTodayCount() > 0) {
            webSocketServer.broadcastAll(buildBroadcastMessage(summary));
        }
    }

    private PlantingPlan buildPlanEntity(Long planId, PlantingPlanSaveRequest request) {
        validateReferences(request);
        int growthCycleDays = planReferenceMapper.selectGrowthCycleDays(request.getCropId());
        LocalDate expectedHarvest = request.getStartDate().plusDays(growthCycleDays);

        if (planReferenceMapper.countOverlappingPlans(request.getPlotId(), request.getStartDate(), expectedHarvest, planId) > 0) {
            throw new BusinessException("The selected plot already has an overlapping planting plan");
        }

        PlantingPlan plan = new PlantingPlan();
        BeanUtils.copyProperties(request, plan);
        plan.setExpectedHarvest(expectedHarvest);
        plan.setStatus(resolvePlanStatus(request.getStartDate(), expectedHarvest, request.getStatus()));
        return plan;
    }

    private void validateReferences(PlantingPlanSaveRequest request) {
        if (planReferenceMapper.countPlotById(request.getPlotId()) == 0) {
            throw new BusinessException(404, "Plot not found");
        }
        if (planReferenceMapper.countCropById(request.getCropId()) == 0) {
            throw new BusinessException(404, "Crop not found");
        }
        if (planReferenceMapper.countUserById(request.getCreatedBy()) == 0) {
            throw new BusinessException(404, "Creator user not found");
        }

        String plotStatus = planReferenceMapper.selectPlotStatus(request.getPlotId());
        if (PLOT_STATUS_FALLOW.equals(plotStatus)) {
            throw new BusinessException("Fallow plots cannot create planting plans");
        }

        BigDecimal plotArea = planReferenceMapper.selectPlotArea(request.getPlotId());
        if (plotArea != null && request.getPlantArea().compareTo(plotArea) > 0) {
            throw new BusinessException("Plant area cannot exceed plot area");
        }
    }

    private PlantingPlan getPlanEntity(Long planId) {
        PlantingPlan plan = plantingPlanMapper.selectById(planId);
        if (plan == null) {
            throw new BusinessException(404, "Planting plan not found");
        }
        return plan;
    }

    private String resolvePlanStatus(LocalDate startDate, LocalDate expectedHarvest, String requestedStatus) {
        if (STATUS_CANCELLED.equals(requestedStatus)) {
            return STATUS_CANCELLED;
        }
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return STATUS_NOT_STARTED;
        }
        if (!today.isBefore(expectedHarvest)) {
            return STATUS_COMPLETED;
        }
        return STATUS_ONGOING;
    }

    private void refreshPlotStatus(Long plotId) {
        String currentStatus = planReferenceMapper.selectPlotStatus(plotId);
        if (PLOT_STATUS_FALLOW.equals(currentStatus) || PLOT_STATUS_MAINTAINING.equals(currentStatus)) {
            return;
        }
        int activeCount = planReferenceMapper.countActivePlanByPlotId(plotId);
        String nextStatus = activeCount > 0 ? PLOT_STATUS_PLANTING : PLOT_STATUS_IDLE;
        if (!nextStatus.equals(currentStatus)) {
            planReferenceMapper.updatePlotStatus(plotId, nextStatus);
        }
    }

    private String buildBroadcastMessage(PlanReminderVO summary) {
        return String.format(
                "种植计划调度完成：进行中%d项，今日启动%d项，7天内待采收%d项，逾期%d项。",
                summary.getOngoingCount(),
                summary.getStartTodayCount(),
                summary.getUpcomingHarvestCount(),
                summary.getOverdueCount()
        );
    }

    private void checkDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
    }
}
