package com.stockanalyzer.analysis.model;

/** 指数日线行情（Feign 传输对象） */
public class IndexDaily {

    private String tsCode;
    private String tradeDate;
    private Double close;
    private Double pctChg;

    public IndexDaily() {
    }

    public String getTsCode() {
        return tsCode;
    }

    public void setTsCode(String tsCode) {
        this.tsCode = tsCode;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getPctChg() {
        return pctChg;
    }

    public void setPctChg(Double pctChg) {
        this.pctChg = pctChg;
    }
}
