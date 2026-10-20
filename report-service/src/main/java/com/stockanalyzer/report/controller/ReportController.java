package com.stockanalyzer.report.controller;

import com.stockanalyzer.report.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 报告 REST 接口 */
@RestController
@RequestMapping("/")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** 生成当日报告 */
    @PostMapping("/daily")
    public Map<String, String> generateDaily() {
        List<String> defaultIndexes = Arrays.asList(
                "000300.SH", "000905.SH", "000001.SH", "399006.SZ", "000688.SH");
        String report = reportService.generateDailyReport(defaultIndexes);
        Map<String, String> result = new HashMap<>();
        result.put("report", report);
        result.put("generatedAt", java.time.LocalDateTime.now().toString());
        return result;
    }

    /** 查看最新报告（简化版） */
    @GetMapping("/latest")
    public Map<String, String> latest() {
        return generateDaily();
    }
}
