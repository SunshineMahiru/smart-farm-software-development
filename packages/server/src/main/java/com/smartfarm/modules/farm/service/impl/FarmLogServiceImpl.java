package com.smartfarm.modules.farm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.farm.dto.FarmLogSaveRequest;
import com.smartfarm.modules.farm.entity.FarmLog;
import com.smartfarm.modules.farm.mapper.FarmLogMapper;
import com.smartfarm.modules.farm.mapper.FarmReferenceMapper;
import com.smartfarm.modules.farm.service.FarmLogService;
import com.smartfarm.modules.farm.vo.FarmLogVO;
import com.smartfarm.modules.farm.vo.LogSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FarmLogServiceImpl implements FarmLogService {

    private final FarmLogMapper farmLogMapper;
    private final FarmReferenceMapper farmReferenceMapper;

    @Override
    public IPage<FarmLogVO> pageLogs(long pageNum, long pageSize, Long planId, Long operatorId,
                                     String operationType, LocalDate startDate, LocalDate endDate) {
        checkDateRange(startDate, endDate);
        return farmLogMapper.selectLogPage(new Page<>(pageNum, pageSize), planId, operatorId, operationType, startDate, endDate);
    }

    @Override
    public FarmLogVO getLogById(Long logId) {
        FarmLogVO log = farmLogMapper.selectLogById(logId);
        if (log == null) {
            throw new BusinessException(404, "Farm log not found");
        }
        return log;
    }

    @Override
    public void createLog(FarmLogSaveRequest request) {
        validateReferences(request.getPlanId(), request.getOperatorId());
        FarmLog log = new FarmLog();
        BeanUtils.copyProperties(request, log);
        farmLogMapper.insert(log);
    }

    @Override
    public void updateLog(Long logId, FarmLogSaveRequest request) {
        validateReferences(request.getPlanId(), request.getOperatorId());
        FarmLog log = getEntityById(logId);
        BeanUtils.copyProperties(request, log);
        farmLogMapper.updateById(log);
    }

    @Override
    public void deleteLog(Long logId) {
        getEntityById(logId);
        farmLogMapper.deleteById(logId);
    }

    @Override
    public List<LogSummaryVO> getLogSummary(LocalDate startDate, LocalDate endDate) {
        checkDateRange(startDate, endDate);
        return farmLogMapper.selectLogSummary(startDate, endDate);
    }

    private FarmLog getEntityById(Long logId) {
        FarmLog log = farmLogMapper.selectById(logId);
        if (log == null) {
            throw new BusinessException(404, "Farm log not found");
        }
        return log;
    }

    private void validateReferences(Long planId, Long operatorId) {
        if (farmReferenceMapper.countPlanById(planId) == 0) {
            throw new BusinessException(404, "Planting plan not found");
        }
        if (farmReferenceMapper.countUserById(operatorId) == 0) {
            throw new BusinessException(404, "Operator user not found");
        }
    }

    private void checkDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }
    }
}
