package com.stockanalyzer.analysis.controller;

import com.stockanalyzer.analysis.model.IndexSummary;
import com.stockanalyzer.analysis.service.IndexAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 指数分析 REST 接口 */
@RestController
@RequestMapping("/")
public class AnalysisController {

    private final IndexAnalysisService analysisService;

    public AnalysisController(IndexAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    /** 单个指数分析摘要 */
    @GetMapping("/index/{code}/summary")
    public ResponseEntity<Map<String, Object>> summary(@PathVariable String code) {
        IndexSummary s = analysisService.summary(normalizeCode(code));
        if (s == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toMap(s));
    }

    /** 批量指数分析摘要：codes 以英文逗号分隔 */
    @GetMapping("/index/summaries")
    public List<Map<String, Object>> summaries(
            @org.springframework.web.bind.annotation.RequestParam String codes) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String raw : codes.split(",")) {
            if (raw.trim().isEmpty()) {
                continue;
            }
            IndexSummary s = analysisService.summary(normalizeCode(raw));
            if (s != null) {
                result.add(toMap(s));
            }
        }
        return result;
    }

    /** 代码归一化：允许 000300、000300.SH、000300SH 混输 */
    private String normalizeCode(String raw) {
        String code = raw.trim().toUpperCase();
        // 如果已经是标准格式（包含.），直接返回
        if (code.contains(".")) {
            return code;
        }
        // 6位数字代码添加后缀
        if (code.matches("^\\d{6}$")) {
            return code.startsWith("39") ? code + ".SZ" : code + ".SH";
        }
        // 处理 000300SH 格式
        if (code.endsWith("SH")) {
            return code.replace("SH", ".SH");
        }
        if (code.endsWith("SZ")) {
            return code.replace("SZ", ".SZ");
        }
        return code;
    }

    private Map<String, Object> toMap(IndexSummary s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tsCode", s.getTsCode());
        m.put("latestDate", s.getLatestDate());
        m.put("latestClose", s.getLatestClose());
        m.put("latestPctChg", s.getLatestPctChg());
        m.put("ma5", s.getMa5());
        m.put("ma20", s.getMa20());
        m.put("ma60", s.getMa60());
        m.put("change20d", s.getChange20d());
        m.put("change60d", s.getChange60d());
        m.put("volatility20d", s.getVolatility20d());
        m.put("ma5OverMa20", s.getMa5OverMa20());
        m.put("ma20OverMa60", s.getMa20OverMa60());
        m.put("trend", s.getTrend());
        m.put("bias20", s.getBias20());
        return m;
    }
}
