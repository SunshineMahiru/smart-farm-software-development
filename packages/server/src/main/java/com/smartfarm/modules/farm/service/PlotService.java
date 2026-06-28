package com.smartfarm.modules.farm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.farm.dto.PlotSaveRequest;
import com.smartfarm.modules.farm.entity.Plot;
import com.smartfarm.modules.farm.vo.PlotStatusSummaryVO;

import java.util.List;

public interface PlotService {

    IPage<Plot> pagePlots(long pageNum, long pageSize, String keyword, String status, String soilType, String location);

    Plot getPlotById(Long plotId);

    List<PlotStatusSummaryVO> getStatusSummary();

    void createPlot(PlotSaveRequest request);

    void updatePlot(Long plotId, PlotSaveRequest request);

    void updatePlotStatus(Long plotId, String status);

    void deletePlot(Long plotId);
}
