package com.stockanalyzer.quote.controller;

import com.stockanalyzer.quote.dao.QuoteDao;
import com.stockanalyzer.quote.model.IndexDaily;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public List<IndexDaily> recent(
            @PathVariable String code,
            @RequestParam(defaultValue = "61") int limit) {
        return quoteDao.findRecent(normalizeCode(code), limit);
    }

    /** 按日期升序取某指数 [start, end] 区间日线，用于 K 线图 */
    @GetMapping("/index/{code}/range")
    public List<IndexDaily> range(
            @PathVariable String code,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return quoteDao.findRange(normalizeCode(code), start, end);
    }

    /** 代码归一化：允许 000300、000300.SH、000300SH 混输 */
    private String normalizeCode(String raw) {
        String code = raw.trim().toUpperCase();
        if (code.contains(".")) {
            return code;
        }
        if (code.matches("^\\d{6}$")) {
            return code.startsWith("39") ? code + ".SZ" : code + ".SH";
        }
        if (code.endsWith("SH")) {
            return code.substring(0, code.length() - 2) + ".SH";
        }
        if (code.endsWith("SZ")) {
            return code.substring(0, code.length() - 2) + ".SZ";
        }
        return code;
    }
}
