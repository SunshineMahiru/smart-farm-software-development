package com.smartfarm.modules.farm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.farm.dto.PlotSaveRequest;
import com.smartfarm.modules.farm.entity.Plot;
import com.smartfarm.modules.farm.mapper.PlotMapper;
import com.smartfarm.modules.farm.service.PlotService;
import com.smartfarm.modules.farm.vo.PlotStatusSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PlotServiceImpl implements PlotService {

    private static final Set<String> VALID_STATUSES = Set.of("空闲", "种植中", "休耕", "维护中");

    private final PlotMapper plotMapper;

    @Override
    public IPage<Plot> pagePlots(long pageNum, long pageSize, String keyword, String status, String soilType, String location) {
        validateOptionalStatus(status);
        LambdaQueryWrapper<Plot> wrapper = new LambdaQueryWrapper<Plot>()
                .and(StringUtils.hasText(keyword), q -> q
                        .like(Plot::getPlotName, keyword)
                        .or()
                        .like(Plot::getLocation, keyword)
                        .or()
                        .like(Plot::getSoilType, keyword))
                .eq(StringUtils.hasText(status), Plot::getStatus, status)
                .eq(StringUtils.hasText(soilType), Plot::getSoilType, soilType)
                .like(StringUtils.hasText(location), Plot::getLocation, location)
                .orderByDesc(Plot::getCreatedAt);
        return plotMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Plot getPlotById(Long plotId) {
        return getEntityById(plotId);
    }

    @Override
    public List<PlotStatusSummaryVO> getStatusSummary() {
        return plotMapper.selectStatusSummary();
    }

    @Override
    public void createPlot(PlotSaveRequest request) {
        validateStatus(request.getStatus());
        checkDuplicateName(null, request.getPlotName());
        Plot plot = new Plot();
        BeanUtils.copyProperties(request, plot);
        plotMapper.insert(plot);
    }

    @Override
    public void updatePlot(Long plotId, PlotSaveRequest request) {
        validateStatus(request.getStatus());
        Plot existing = getEntityById(plotId);
        validateStatusTransition(existing.getStatus(), request.getStatus());
        checkDuplicateName(plotId, request.getPlotName());
        BeanUtils.copyProperties(request, existing);
        plotMapper.updateById(existing);
    }

    @Override
    public void updatePlotStatus(Long plotId, String status) {
        validateStatus(status);
        Plot existing = getEntityById(plotId);
        validateStatusTransition(existing.getStatus(), status);
        existing.setStatus(status);
        plotMapper.updateById(existing);
    }

    @Override
    public void deletePlot(Long plotId) {
        getEntityById(plotId);
        ensurePlotNotReferenced(plotId);
        plotMapper.deleteById(plotId);
    }

    private Plot getEntityById(Long plotId) {
        Plot plot = plotMapper.selectById(plotId);
        if (plot == null) {
            throw new BusinessException(404, "Plot not found");
        }
        return plot;
    }

    private void checkDuplicateName(Long plotId, String plotName) {
        Plot plot = plotMapper.selectOne(new LambdaQueryWrapper<Plot>()
                .eq(Plot::getPlotName, plotName)
                .ne(plotId != null, Plot::getPlotId, plotId)
                .last("LIMIT 1"));
        if (plot != null) {
            throw new BusinessException("Plot name already exists");
        }
    }

    private void validateOptionalStatus(String status) {
        if (StringUtils.hasText(status)) {
            validateStatus(status);
        }
    }

    private void validateStatus(String status) {
        if (!VALID_STATUSES.contains(status)) {
            throw new BusinessException("Invalid plot status");
        }
    }

    private void validateStatusTransition(String currentStatus, String targetStatus) {
        if (currentStatus.equals(targetStatus)) {
            return;
        }
        boolean allowed = switch (currentStatus) {
            case "空闲" -> Set.of("种植中", "休耕", "维护中").contains(targetStatus);
            case "种植中" -> Set.of("空闲", "维护中").contains(targetStatus);
            case "休耕" -> Set.of("空闲", "维护中").contains(targetStatus);
            case "维护中" -> "空闲".equals(targetStatus);
            default -> false;
        };
        if (!allowed) {
            throw new BusinessException("Invalid plot status transition: " + currentStatus + " -> " + targetStatus);
        }
    }

    private void ensurePlotNotReferenced(Long plotId) {
        int planCount = plotMapper.countPlantingPlanByPlotId(plotId);
        int irrigationCount = plotMapper.countIrrigationRecordByPlotId(plotId);
        int sensorCount = plotMapper.countSensorByPlotId(plotId);
        int alertCount = plotMapper.countDeviceAlertByPlotId(plotId);
        if (planCount + irrigationCount + sensorCount + alertCount > 0) {
            throw new BusinessException("Plot is referenced by business records and cannot be deleted");
        }
    }
}
