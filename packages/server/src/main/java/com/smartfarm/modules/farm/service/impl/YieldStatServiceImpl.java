package com.smartfarm.modules.farm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.farm.dto.YieldStatSaveRequest;
import com.smartfarm.modules.farm.entity.YieldStat;
import com.smartfarm.modules.farm.mapper.FarmReferenceMapper;
import com.smartfarm.modules.farm.mapper.YieldStatMapper;
import com.smartfarm.modules.farm.service.YieldStatService;
import com.smartfarm.modules.farm.vo.PlanYieldSummaryVO;
import com.smartfarm.modules.farm.vo.YieldStatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class YieldStatServiceImpl implements YieldStatService {

    private final YieldStatMapper yieldStatMapper;
    private final FarmReferenceMapper farmReferenceMapper;

    @Override
    public IPage<YieldStatVO> pageYieldStats(long pageNum, long pageSize, Long planId, String qualityGrade,
                                             LocalDate startDate, LocalDate endDate) {
        checkDateRange(startDate, endDate);
        return yieldStatMapper.selectYieldPage(new Page<>(pageNum, pageSize), planId, qualityGrade, startDate, endDate);
    }

    @Override
    public YieldStatVO getYieldById(Long yieldId) {
        YieldStatVO stat = yieldStatMapper.selectYieldById(yieldId);
        if (stat == null) {
            throw new BusinessException(404, "Yield stat not found");
        }
        return stat;
    }

    @Override
    public void createYield(YieldStatSaveRequest request) {
        validateRequest(request);
        YieldStat stat = new YieldStat();
        BeanUtils.copyProperties(request, stat);
        yieldStatMapper.insert(stat);
    }

    @Override
    public void updateYield(Long yieldId, YieldStatSaveRequest request) {
        validateRequest(request);
        YieldStat stat = getEntityById(yieldId);
        BeanUtils.copyProperties(request, stat);
        yieldStatMapper.updateById(stat);
    }

    @Override
    public void deleteYield(Long yieldId) {
        getEntityById(yieldId);
        yieldStatMapper.deleteById(yieldId);
    }

    @Override
    public List<PlanYieldSummaryVO> getPlanYieldSummary(Long planId) {
        if (planId != null && farmReferenceMapper.countPlanById(planId) == 0) {
            throw new BusinessException(404, "Planting plan not found");
        }
        return yieldStatMapper.selectPlanYieldSummary(planId);
    }

    private YieldStat getEntityById(Long yieldId) {
        YieldStat stat = yieldStatMapper.selectById(yieldId);
        if (stat == null) {
            throw new BusinessException(404, "Yield stat not found");
        }
        return stat;
    }

    private void validateRequest(YieldStatSaveRequest request) {
        if (farmReferenceMapper.countPlanById(request.getPlanId()) == 0) {
            throw new BusinessException(404, "Planting plan not found");
        }
        if (request.getYieldWeight() != null && request.getYieldWeight().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Yield weight cannot be negative");
        }
    }

    private void checkDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
    }
}
