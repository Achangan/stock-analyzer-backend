package com.stockanalyzer.analysis.feign;

import com.stockanalyzer.analysis.model.IndexDaily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** 行情服务 Feign 客户端 */
@FeignClient(name = "quote-service")
public interface QuoteFeignClient {

    @GetMapping("/index/{code}/recent")
    List<IndexDaily> getRecentQuotes(@PathVariable("code") String code, @RequestParam("limit") int limit);
}
