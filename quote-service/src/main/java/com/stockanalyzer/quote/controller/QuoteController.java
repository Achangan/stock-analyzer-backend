package com.stockanalyzer.quote.controller;

import com.stockanalyzer.quote.dao.QuoteDao;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 行情数据 REST 接口 */
@RestController
@RequestMapping("/")
public class QuoteController {

    private final QuoteDao quoteDao;

    public QuoteController(QuoteDao quoteDao) {
        this.quoteDao = quoteDao;
    }

    /** 全部已入库指数列表 */
    @GetMapping("/index/list")
    public List<String> indexList() {
        return quoteDao.findIndexCodes();
    }

    /** 数据覆盖情况 */
    @GetMapping("/coverage")
    public List<QuoteDao.IndexCoverage> coverage() {
        return quoteDao.findCoverage();
    }

    /** 按日期降序取某指数最近 N 个交易日 */
    @GetMapping("/index/{code}/recent")
    public List<com.stockanalyzer.quote.model.IndexDaily> recent(
            @PathVariable String code,
            @org.springframework.web.bind.annotation.RequestParam(defaultValue = "61") int limit) {
        return quoteDao.findRecent(code, limit);
    }
}
