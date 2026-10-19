package com.stockanalyzer.report.service;

import com.stockanalyzer.report.feign.AnalysisFeignClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/** 报告生成服务 */
@Service
public class ReportService {

    private final AnalysisFeignClient analysisFeignClient;

    public ReportService(AnalysisFeignClient analysisFeignClient) {
        this.analysisFeignClient = analysisFeignClient;
    }

    /** 生成每日分析报告 */
    public String generateDailyReport(List<String> indexCodes) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 每日指数分析报告\n\n");
        sb.append("生成时间：").append(java.time.LocalDateTime.now()).append("\n\n");
        sb.append("| 指数 | 最新收盘 | 涨跌幅 | MA5 | MA20 | MA60 | 20日涨幅 | 60日涨幅 | 20日波动率 |\n");
        sb.append("|------|----------|--------|-----|------|------|----------|----------|------------|\n");

        for (String code : indexCodes) {
            try {
                Map<String, Object> summary = analysisFeignClient.getIndexSummary(code);
                if (summary != null) {
                    sb.append("| ").append(code).append(" | ")
                            .append(summary.get("latestClose")).append(" | ")
                            .append(summary.get("latestPctChg")).append("% | ")
                            .append(summary.get("ma5")).append(" | ")
                            .append(summary.get("ma20")).append(" | ")
                            .append(summary.get("ma60")).append(" | ")
                            .append(summary.get("change20d")).append("% | ")
                            .append(summary.get("change60d")).append("% | ")
                            .append(summary.get("volatility20d")).append("% |\n");
                }
            } catch (Exception e) {
                sb.append("| ").append(code).append(" | 数据获取失败 | - | - | - | - | - | - | - |\n");
            }
        }

        sb.append("\n---\n");
        sb.append("*本报告由 stock-analyzer 自动生成，仅供参考，不构成投资建议。*\n");
        return sb.toString();
    }
}
