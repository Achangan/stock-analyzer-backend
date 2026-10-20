package com.stockanalyzer.analysis.service;

import com.stockanalyzer.analysis.feign.QuoteFeignClient;
import com.stockanalyzer.analysis.model.IndexDaily;
import com.stockanalyzer.analysis.model.IndexSummary;
import org.springframework.stereotype.Service;

import java.util.List;

/** 指数分析：均线、区间涨跌幅、年化波动率 */
@Service
public class IndexAnalysisService {

    private static final int MA5_WINDOW = 5;
    private static final int MA20_WINDOW = 20;
    private static final int MA60_WINDOW = 60;
    private static final int TRADING_DAYS_PER_YEAR = 252;

    private final QuoteFeignClient quoteFeignClient;

    public IndexAnalysisService(QuoteFeignClient quoteFeignClient) {
        this.quoteFeignClient = quoteFeignClient;
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IndexAnalysisService.class);

    public IndexSummary summary(String tsCode) {
        // 61 个点：60 个用于 MA60，再往前 1 个用于计算 60 日涨跌幅
        log.info("Fetching recent quotes for: {}", tsCode);
        List<IndexDaily> rows = quoteFeignClient.getRecentQuotes(tsCode, MA60_WINDOW + 1);
        log.info("Received {} rows", rows == null ? 0 : rows.size());
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        IndexSummary s = new IndexSummary();
        IndexDaily latest = rows.get(0);
        s.setTsCode(latest.getTsCode());
        s.setLatestDate(latest.getTradeDate());
        s.setLatestClose(latest.getClose());
        s.setLatestPctChg(latest.getPctChg());

        s.setMa5(ma(rows, MA5_WINDOW));
        s.setMa20(ma(rows, MA20_WINDOW));
        s.setMa60(ma(rows, MA60_WINDOW));

        s.setChange20d(changeOver(rows, MA20_WINDOW));
        s.setChange60d(changeOver(rows, MA60_WINDOW));
        s.setVolatility20d(annualizedVolatility(rows, MA20_WINDOW));

        // 均线关系与趋势形态
        if (s.getMa5() != null && s.getMa20() != null) {
            s.setMa5OverMa20(s.getMa5() > s.getMa20());
        }
        if (s.getMa20() != null && s.getMa60() != null) {
            s.setMa20OverMa60(s.getMa20() > s.getMa60());
        }
        s.setTrend(trend(s.getMa5(), s.getMa20(), s.getMa60()));
        if (s.getMa20() != null && s.getMa20() != 0.0) {
            s.setBias20(round2((s.getLatestClose() / s.getMa20() - 1) * 100));
        }
        return s;
    }

    /** 趋势形态：MA5 > MA20 > MA60 为多头排列，反之空头排列，其余震荡 */
    private String trend(Double ma5, Double ma20, Double ma60) {
        if (ma5 == null || ma20 == null || ma60 == null) {
            return null;
        }
        if (ma5 > ma20 && ma20 > ma60) {
            return "多头排列";
        }
        if (ma5 < ma20 && ma20 < ma60) {
            return "空头排列";
        }
        return "震荡";
    }

    /** N 日均线：rows 按日期降序，取前 N 个 close 的均值；数据不足返回 null */
    private Double ma(List<IndexDaily> rowsDesc, int n) {
        if (rowsDesc.size() < n) {
            return null;
        }
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += rowsDesc.get(i).getClose();
        }
        return round2(sum / n);
    }

    /** 近 N 个交易日累计涨跌幅（%）：最新收盘 vs N 个交易日前收盘 */
    private Double changeOver(List<IndexDaily> rowsDesc, int n) {
        if (rowsDesc.size() <= n) {
            return null;
        }
        double latest = rowsDesc.get(0).getClose();
        double before = rowsDesc.get(n).getClose();
        return round2((latest / before - 1) * 100);
    }

    /** 近 N 个交易日年化波动率（%）：日涨跌幅标准差 × √252；样本不足 10 个返回 null */
    private Double annualizedVolatility(List<IndexDaily> rowsDesc, int n) {
        int sample = Math.min(n, rowsDesc.size());
        if (sample < 10) {
            return null;
        }
        double[] rets = new double[sample - 1];
        for (int i = 0; i < sample - 1; i++) {
            double cur = rowsDesc.get(i).getClose();
            double prev = rowsDesc.get(i + 1).getClose();
            rets[i] = cur / prev - 1;
        }
        double mean = 0;
        for (double r : rets) {
            mean += r;
        }
        mean /= rets.length;
        double var = 0;
        for (double r : rets) {
            var += (r - mean) * (r - mean);
        }
        var /= (rets.length - 1);
        double dailySigma = Math.sqrt(var);
        return round2(dailySigma * Math.sqrt(TRADING_DAYS_PER_YEAR) * 100);
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
