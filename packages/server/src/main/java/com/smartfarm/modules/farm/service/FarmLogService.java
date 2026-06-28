package com.smartfarm.modules.farm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.farm.dto.FarmLogSaveRequest;
import com.smartfarm.modules.farm.vo.FarmLogVO;
import com.smartfarm.modules.farm.vo.LogSummaryVO;

import java.time.LocalDate;
import java.util.List;

public interface FarmLogService {

    IPage<FarmLogVO> pageLogs(long pageNum, long pageSize, Long planId, Long operatorId,
                              String operationType, LocalDate startDate, LocalDate endDate);

    FarmLogVO getLogById(Long logId);

    void createLog(FarmLogSaveRequest request);

    void updateLog(Long logId, FarmLogSaveRequest request);

    void deleteLog(Long logId);

    List<LogSummaryVO> getLogSummary(LocalDate startDate, LocalDate endDate);
}
