import os
import sqlite3

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
conn = sqlite3.connect(os.path.join(BASE_DIR, "data", "stock.db"))

print("指数覆盖：")
for r in conn.execute(
    "SELECT ts_code, MIN(trade_date), MAX(trade_date), COUNT(*) FROM index_daily GROUP BY ts_code"
):
    print(" ", r)

print("沪深300最近3个交易日：")
for r in conn.execute(
    "SELECT trade_date, close, pct_chg FROM index_daily "
    "WHERE ts_code='000300.SH' ORDER BY trade_date DESC LIMIT 3"
):
    print(" ", r)

print("采集日志：")
for r in conn.execute(
    "SELECT run_at, source, target, rows, status FROM collect_log ORDER BY id DESC LIMIT 5"
):
    print(" ", r)
