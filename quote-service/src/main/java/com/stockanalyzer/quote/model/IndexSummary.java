package com.stockanalyzer.quote.model;

/** 指数分析摘要 */
public class IndexSummary {

    private String tsCode;
    private String latestDate;
    private Double latestClose;
    private Double latestPctChg;
    private Double ma5;
    private Double ma20;
    private Double ma60;
    private Double change20d;
    private Double change60d;
    private Double volatility20d;

    public String getTsCode() {
        return tsCode;
    }

    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
    }

    public String getLatestDate() {
        return latestDate;
    }

    public void setLatestDate(String latestDate) {
        this.latestDate = latestDate;
    }

    public Double getLatestClose() {
        return latestClose;
    }

    public void setLatestClose(Double latestClose) {
        this.latestClose = latestClose;
    }

    public Double getLatestPctChg() {
        return latestPctChg;
    }

    public void setLatestPctChg(Double latestPctChg) {
        this.latestPctChg = latestPctChg;
    }

    public Double getMa5() {
        return ma5;
    }

    public void setMa5(Double ma5) {
        this.ma5 = ma5;
    }

    public Double getMa20() {
        return ma20;
    }

    public void setMa20(Double ma20) {
        this.ma20 = ma20;
    }

    public Double getMa60() {
        return ma60;
    }

    public void setMa60(Double ma60) {
        this.ma60 = ma60;
    }

    public Double getChange20d() {
        return change20d;
    }

    public void setChange20d(Double change20d) {
        this.change20d = change20d;
    }

    public Double getChange60d() {
        return change60d;
    }

    public void setChange60d(Double change60d) {
        this.change60d = change60d;
    }

    public Double getVolatility20d() {
        return volatility20d;
    }

    public void setVolatility20d(Double volatility20d) {
        this.volatility20d = volatility20d;
    }
}
