package com.stockanalyzer.report.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/** 分析服务 Feign 客户端 */
@FeignClient(name = "analysis-service")
public interface AnalysisFeignClient {

    @GetMapping("/index/{code}/summary")
    Map<String, Object> getIndexSummary(@PathVariable("code") String code);
}
