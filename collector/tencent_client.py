"""腾讯行情客户端：免费无需 token，指数日线兜底通道

定位：覆盖 baostock 缺失的指数（如科创50 sh.000688），并作为
Tushare（积分不足）/ Baostock（覆盖缺口）之后的最后兜底。
注意：腾讯日K不含成交额，amount 置 None；vol 单位为手，入库前换算为股
（与 baostock 口径一致）；pre_close 取前一日收盘（指数无复权）。
"""
import json
import urllib.request
from datetime import datetime, timedelta

import config


def _sym(ts_code: str) -> str:
    """000688.SH -> sh000688"""
    code, market = ts_code.split(".")
    return ("sh" if market == "SH" else "sz") + code


def _fetch_bars(ts_code: str, start: str, end: str, count: int = 1023) -> list[list]:
    """腾讯日K原始数组 [date, open, close, high, low, vol(手)]，日期升序"""
    url = (
        "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get"
        f"?param={_sym(ts_code)},day,{start},{end},{count},"
    )
    req = urllib.request.Request(url)  # 不带 UA（实测带浏览器头也会被部分节点断连）
    last_err: Exception | None = None
    for _ in range(3):
        try:
            with urllib.request.urlopen(req, timeout=20) as r:
                data = json.loads(r.read().decode("utf-8"))
            break
        except Exception as e:  # 网络抖动重试
            last_err = e
    else:
        raise RuntimeError(f"tencent 请求失败: {last_err}")
    node = (data.get("data") or {}).get(_sym(ts_code)) or {}
    return node.get("day") or node.get("qfqday") or []


def fetch_index_daily(conn, db, ts_code: str, start: str | None = None, end: str | None = None) -> int:
    """指数日线兜底采集（增量：默认从库内最新日期次日开始）"""
    if start is None:
        last = db.last_trade_date(conn, "index_daily", ts_code)
        start = (datetime.strptime(last, "%Y-%m-%d") + timedelta(days=1)).strftime("%Y-%m-%d") if last else config.HISTORY_START
    end = end or datetime.now().strftime("%Y-%m-%d")
    if start > end:
        return 0
    # 向前多取 7 天做缓冲，保证窗口首行有前收盘可依
    buf = (datetime.strptime(start, "%Y-%m-%d") - timedelta(days=7)).strftime("%Y-%m-%d")
    all_bars = _fetch_bars(ts_code, buf, end)

    rows = []
    for i, b in enumerate(all_bars):
        if b[0] < start:
            continue
        if b[0] > end:
            break
        c = float(b[2])  # 列序：date, open, close, high, low, vol
        pre = float(all_bars[i - 1][2]) if i > 0 else None
        pct = round((c / pre - 1) * 100, 4) if pre else None
        rows.append((ts_code, b[0], float(b[1]), float(b[3]), float(b[4]), c,
                     pre, pct, float(b[5]) * 100.0, None))
    if not rows:
        db.log_collect(conn, "tencent", f"index_daily:{ts_code}", 0, "success", "无新数据")
        return 0
    n = db.upsert_many(conn, "index_daily",
                       ["ts_code", "trade_date", "open", "high", "low", "close",
                        "pre_close", "pct_chg", "vol", "amount"], rows)
    db.log_collect(conn, "tencent", f"index_daily:{ts_code}", n, "success")
    return n
