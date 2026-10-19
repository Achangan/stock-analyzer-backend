"""每日采集入口：收盘后运行一次
用法：
    python run_daily.py            # 增量采集（指数 + 当日全市场日线，Tushare 优先）
    python run_daily.py --backfill # 历史回填（Baostock，默认从 HISTORY_START 起）
"""
import argparse
import sys
from datetime import datetime

import baostock_client
import config
import db as dbm
import tushare_client


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--backfill", action="store_true", help="用 Baostock 回填历史数据")
    args = parser.parse_args()

    conn = dbm.get_conn()
    dbm.init_db(conn)

    today = datetime.now().strftime("%Y-%m-%d")
    has_token = bool(config.TUSHARE_TOKEN)

    if args.backfill or not has_token:
        if not has_token:
            print("[info] 未配置 TUSHARE_TOKEN，使用 Baostock 通道")
        total = 0
        for idx in config.DEFAULT_INDEXES:
            n = baostock_client.fetch_index_daily(conn, dbm, idx)
            print(f"[baostock] index {idx}: +{n} 行")
            total += n
        print(f"[done] 指数采集完成，共 {total} 行（{today}）")
        return 0

    # Tushare 通道：先更新交易日历，确认今天是交易日再拉全市场
    tushare_client.fetch_trade_cal(conn, dbm)
    is_open = conn.execute(
        "SELECT is_open FROM trade_cal WHERE cal_date=?", (today,)
    ).fetchone()
    if is_open and is_open[0] == 0:
        print(f"[skip] {today} 非交易日")
        return 0

    for idx in config.DEFAULT_INDEXES:
        n = tushare_client.fetch_index_daily(conn, dbm, idx)
        print(f"[tushare] index {idx}: +{n} 行")

    n = tushare_client.fetch_daily(conn, dbm, today)
    print(f"[tushare] 全市场日线 {today}: +{n} 行")
    print("[done] 每日采集完成")
    return 0


if __name__ == "__main__":
    sys.exit(main())
