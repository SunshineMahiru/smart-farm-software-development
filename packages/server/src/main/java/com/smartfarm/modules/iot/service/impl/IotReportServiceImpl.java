package com.smartfarm.modules.iot.service.impl;

import com.smartfarm.common.websocket.WebSocketServer;
import com.smartfarm.modules.iot.mapper.IotReportMapper;
import com.smartfarm.modules.iot.service.IotReportService;
import com.smartfarm.modules.iot.vo.IotDailyAggregateVO;
import com.smartfarm.modules.iot.vo.IotDailyReportVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IotReportServiceImpl implements IotReportService {

    private final IotReportMapper iotReportMapper;
    private final WebSocketServer webSocketServer;

    @Override
    public IotDailyReportVO generateDailyReport() {
        LocalDate reportDate = LocalDate.now();
        IotDailyReportVO existingReport = iotReportMapper.selectByReportDate(reportDate);
        if (existingReport != null) {
            return existingReport;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        IotDailyAggregateVO aggregate = iotReportMapper.selectAggregate(startTime, endTime);
        if (aggregate == null) {
            aggregate = new IotDailyAggregateVO();
        }

        IotDailyReportVO report = new IotDailyReportVO();
        report.setReportDate(reportDate);
        report.setGeneratedAt(endTime);
        report.setDataStartTime(startTime);
        report.setDataEndTime(endTime);
        report.setAvgTemperature(aggregate.getAvgTemperature());
        report.setMinTemperature(aggregate.getMinTemperature());
        report.setMaxTemperature(aggregate.getMaxTemperature());
        report.setAvgHumidity(aggregate.getAvgHumidity());
        report.setMinHumidity(aggregate.getMinHumidity());
        report.setMaxHumidity(aggregate.getMaxHumidity());
        report.setAvgSoilMoisture(aggregate.getAvgSoilMoisture());
        report.setAvgLightIntensity(aggregate.getAvgLightIntensity());
        report.setTotalAlerts(defaultInt(aggregate.getTotalAlerts()));
        report.setPendingAlerts(defaultInt(aggregate.getPendingAlerts()));
        report.setReportContent(buildReportContent(report));

        iotReportMapper.insert(report);
        webSocketServer.broadcastAll("【农场微气候日报】" + report.getReportContent());
        return report;
    }

    @Override
    public IotDailyReportVO getLatestReport() {
        return iotReportMapper.selectLatestReport();
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String buildReportContent(IotDailyReportVO report) {
        StringBuilder builder = new StringBuilder();
        builder.append("过去24小时农场环境概览：");
        builder.append("平均温度").append(formatDecimal(report.getAvgTemperature())).append("℃，");
        builder.append("温度区间").append(formatDecimal(report.getMinTemperature())).append("~")
                .append(formatDecimal(report.getMaxTemperature())).append("℃；");
        builder.append("平均湿度").append(formatDecimal(report.getAvgHumidity())).append("%，");
        builder.append("湿度区间").append(formatDecimal(report.getMinHumidity())).append("~")
                .append(formatDecimal(report.getMaxHumidity())).append("%；");
        builder.append("平均土壤湿度").append(formatDecimal(report.getAvgSoilMoisture())).append("%，");
        builder.append("平均光照强度").append(formatDecimal(report.getAvgLightIntensity())).append("lux。");

        if (report.getPendingAlerts() > 0) {
            builder.append("当前存在").append(report.getPendingAlerts())
                    .append("条未处理告警，建议优先检查对应地块传感器状态与环境异常点。");
        } else if (report.getTotalAlerts() > 0) {
            builder.append("近24小时共记录").append(report.getTotalAlerts())
                    .append("条告警，均已处理，可继续观察波动趋势。");
        } else {
            builder.append("近24小时未发现异常告警，整体环境较稳定。");
        }

        if (greaterThan(report.getMaxTemperature(), new BigDecimal("32"))) {
            builder.append("高温风险偏高，建议加强通风或灌溉降温。");
        }
        if (lessThan(report.getMinHumidity(), new BigDecimal("35"))) {
            builder.append("空气湿度偏低，需关注作物水分胁迫。");
        }
        if (lessThan(report.getAvgSoilMoisture(), new BigDecimal("40"))) {
            builder.append("土壤湿度处于偏低水平，建议安排补水。");
        }

        return builder.toString();
    }

    private boolean greaterThan(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) > 0;
    }

    private boolean lessThan(BigDecimal value, BigDecimal threshold) {
        return value != null && value.compareTo(threshold) < 0;
    }

    private String formatDecimal(BigDecimal value) {
        return value == null ? "--" : value.stripTrailingZeros().toPlainString();
    }
}
