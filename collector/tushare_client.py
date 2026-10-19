"""Tushare Pro 客户端：日线行情、交易日历（需要 token）"""
from datetime import datetime, timedelta

import config


def _pro():
    if not config.TUSHARE_TOKEN:
        raise RuntimeError("未配置 TUSHARE_TOKEN：请在 config.py 填入或设置环境变量 TUSHARE_TOKEN")
    import tushare as ts
    ts.set_token(config.TUSHARE_TOKEN)
    return ts.pro_api()


def _fmt(d: str) -> str:
    return d.replace("-", "")


def fetch_trade_cal(conn, db, days_back: int = 30, days_forward: int = 30) -> int:
    """同步交易日历（前后各约一个月）"""
    pro = _pro()
    start = _fmt((datetime.now() - timedelta(days=days_back)).strftime("%Y-%m-%d"))
    end = _fmt((datetime.now() + timedelta(days=days_forward)).strftime("%Y-%m-%d"))
    df = pro.trade_cal(exchange="SSE", start_date=start, end_date=end)
    rows = [
        (f"{r.cal_date[:4]}-{r.cal_date[4:6]}-{r.cal_date[6:]}", int(r.is_open))
        for r in df.itertuples()
    ]
    n = db.upsert_many(conn, "trade_cal", ["cal_date", "is_open"], rows)
    db.log_collect(conn, "tushare", "trade_cal", n, "success")
    return n


def fetch_index_daily(conn, db, ts_code: str, start: str | None = None, end: str | None = None) -> int:
    """指数日线，增量采集（默认从库内最新日期次日开始）"""
    pro = _pro()
    if start is None:
        last = db.last_trade_date(conn, "index_daily", ts_code)
        start = (datetime.strptime(last, "%Y-%m-%d") + timedelta(days=1)).strftime("%Y-%m-%d") if last else config.HISTORY_START
    end = end or datetime.now().strftime("%Y-%m-%d")
    if start > end:
        return 0
    df = pro.index_daily(ts_code=ts_code, start_date=_fmt(start), end_date=_fmt(end))
    if df is None or df.empty:
        db.log_collect(conn, "tushare", f"index_daily:{ts_code}", 0, "success", "无新数据")
        return 0
    rows = [
        (r.ts_code, f"{r.trade_date[:4]}-{r.trade_date[4:6]}-{r.trade_date[6:]}",
         r.open, r.high, r.low, r.close, r.pre_close, r.pct_chg, r.vol, r.amount)
        for r in df.itertuples()
    ]
    n = db.upsert_many(conn, "index_daily",
                       ["ts_code", "trade_date", "open", "high", "low", "close",
                        "pre_close", "pct_chg", "vol", "amount"], rows)
    db.log_collect(conn, "tushare", f"index_daily:{ts_code}", n, "success")
    return n


def fetch_daily(conn, db, trade_date: str) -> int:
    """按交易日拉全市场个股日线（Tushare 推荐用法，一次一个交易日）"""
    pro = _pro()
    df = pro.daily(trade_date=_fmt(trade_date))
    if df is None or df.empty:
        db.log_collect(conn, "tushare", f"daily:{trade_date}", 0, "success", "非交易日或无数据")
        return 0
    rows = [
        (r.ts_code, f"{r.trade_date[:4]}-{r.trade_date[4:6]}-{r.trade_date[6:]}",
         r.open, r.high, r.low, r.close, r.pre_close, r.change, r.pct_chg, r.vol, r.amount)
        for r in df.itertuples()
    ]
    n = db.upsert_many(conn, "daily_quote",
                       ["ts_code", "trade_date", "open", "high", "low", "close",
                        "pre_close", "change", "pct_chg", "vol", "amount"], rows)
    db.log_collect(conn, "tushare", f"daily:{trade_date}", n, "success")
    return n
