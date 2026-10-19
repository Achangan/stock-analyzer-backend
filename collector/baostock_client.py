"""Baostock 客户端：免费无需 token，用于历史回填与 Tushare 不可用时的兜底"""
from datetime import datetime, timedelta

import config


def _to_bs_code(ts_code: str) -> str:
    """600519.SH -> sh.600519"""
    code, market = ts_code.split(".")
    return f"{market.lower()}.{code}"


def _from_bs_date(d: str) -> str:
    return d  # baostock 返回即 YYYY-MM-DD


def fetch_index_daily(conn, db, ts_code: str, start: str | None = None, end: str | None = None) -> int:
    """指数日线回填（前复权不适用指数，指数无复权概念）"""
    import baostock as bs
    start = start or config.HISTORY_START
    end = end or datetime.now().strftime("%Y-%m-%d")

    lg = bs.login()
    if lg.error_code != "0":
        db.log_collect(conn, "baostock", f"index_daily:{ts_code}", 0, "failed", lg.error_msg)
        return 0
    try:
        rs = bs.query_history_k_data_plus(
            _to_bs_code(ts_code),
            "date,code,open,high,low,close,preclose,volume,amount,pctChg",
            start_date=start, end_date=end, frequency="d",
        )
        rows = []
        while rs.error_code == "0" and rs.next():
            d = rs.get_row_data()
            rows.append((ts_code, d[0], _f(d[2]), _f(d[3]), _f(d[4]), _f(d[5]),
                         _f(d[6]), _f(d[9]), _f(d[7]), _f(d[8])))
        n = db.upsert_many(conn, "index_daily",
                           ["ts_code", "trade_date", "open", "high", "low", "close",
                            "pre_close", "pct_chg", "vol", "amount"], rows)
        db.log_collect(conn, "baostock", f"index_daily:{ts_code}", n, "success")
        return n
    finally:
        bs.logout()


def fetch_stock_daily(conn, db, ts_code: str, start: str | None = None, end: str | None = None) -> int:
    """个股日线回填（后复权 hfq，适合长期分析）"""
    import baostock as bs
    start = start or config.HISTORY_START
    end = end or datetime.now().strftime("%Y-%m-%d")

    lg = bs.login()
    if lg.error_code != "0":
        db.log_collect(conn, "baostock", f"daily_quote:{ts_code}", 0, "failed", lg.error_msg)
        return 0
    try:
        rs = bs.query_history_k_data_plus(
            _to_bs_code(ts_code),
            "date,code,open,high,low,close,preclose,volume,amount,pctChg",
            start_date=start, end_date=end, frequency="d", adjustflag="2",  # 2=前复权
        )
        rows = []
        while rs.error_code == "0" and rs.next():
            d = rs.get_row_data()
            rows.append((ts_code, d[0], _f(d[2]), _f(d[3]), _f(d[4]), _f(d[5]),
                         _f(d[6]), _f(d[9]), _f(d[7]), _f(d[8])))
        n = db.upsert_many(conn, "daily_quote",
                           ["ts_code", "trade_date", "open", "high", "low", "close",
                            "pre_close", "change", "pct_chg", "vol", "amount"], rows)
        db.log_collect(conn, "baostock", f"daily_quote:{ts_code}", n, "success")
        return n
    finally:
        bs.logout()


def _f(v: str):
    """空串转 None，其余转 float"""
    return float(v) if v not in ("", None) else None
