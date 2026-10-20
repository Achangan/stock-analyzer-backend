package com.stockanalyzer.quote.model;

/** 指数日线行情 */
public class IndexDaily {

    private String tsCode;
    private String tradeDate;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Double preClose;
    private Double pctChg;
    private Double vol;
    private Double amount;

    public IndexDaily() {
    }

    public IndexDaily(String tsCode, String tradeDate, Double open, Double high, Double low,
                      Double close, Double preClose, Double pctChg, Double vol, Double amount) {
        this.tsCode = tsCode;
        this.tradeDate = tradeDate;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.preClose = preClose;
        this.pctChg = pctChg;
        this.vol = vol;
        this.amount = amount;
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

    public Double getOpen() {
        return open;
    }

    public void setOpen(Double open) {
        this.open = open;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getPreClose() {
        return preClose;
    }

    public void setPreClose(Double preClose) {
        this.preClose = preClose;
    }

    public Double getPctChg() {
        return pctChg;
    }

    public void setPctChg(Double pctChg) {
        this.pctChg = pctChg;
    }

    public Double getVol() {
        return vol;
    }

    public void setVol(Double vol) {
        this.vol = vol;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
