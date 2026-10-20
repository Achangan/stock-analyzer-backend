"""每日采集入口：收盘后运行一次
用法：
    python run_daily.py            # 增量采集（指数 + 当日全市场日线，Tushare 优先）
    python run_daily.py --backfill # 历史回填（从 HISTORY_START 起，可反复执行）
"""
import argparse
import sys
from datetime import datetime

import baostock_client
import config
import db_mysql as dbm
import tencent_client
import tushare_client


def fetch_index_with_fallback(conn, db, idx: str, start: str | None = None) -> int:
    """单指数采集：Tushare → Baostock → 腾讯。

    任一通道拿到 >0 行即成功返回；0 行或异常继续尝试下一通道
    （科创50等指数仅腾讯覆盖，靠兜底链保证不漏）。
    """
    channels: list[tuple[str, object]] = []
    if config.TUSHARE_TOKEN:
        channels.append(("tushare", tushare_client.fetch_index_daily))
    channels.append(("baostock", baostock_client.fetch_index_daily))
    channels.append(("tencent", tencent_client.fetch_index_daily))

    for name, fn in channels:
        try:
            n = fn(conn, db, idx, start=start)
            print(f"[{name}] index {idx}: +{n} 行")
            if n > 0:
                return n
        except Exception as e:
            print(f"[{name}] index {idx} 通道失败: {e}")
    print(f"[warn] index {idx} 所有通道均无新数据")
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--backfill", action="store_true", help="从 HISTORY_START 全量回填（幂等，可反复执行）")
    args = parser.parse_args()

    conn = dbm.get_conn()
    dbm.init_db(conn)

    today = datetime.now().strftime("%Y-%m-%d")
    start = config.HISTORY_START if args.backfill else None

    # 交易日历：失败不阻断指数采集
    if config.TUSHARE_TOKEN:
        try:
            tushare_client.fetch_trade_cal(conn, dbm)
        except Exception as e:
            print(f"[warn] 交易日历更新失败（不影响指数采集）: {e}")

    if not config.TUSHARE_TOKEN:
        print("[info] 未配置 TUSHARE_TOKEN，走 Baostock/腾讯兜底通道")
    elif args.backfill:
        print(f"[info] 回填模式：从 {config.HISTORY_START} 全量拉取")

    total = 0
    for idx in config.DEFAULT_INDEXES:
        total += fetch_index_with_fallback(conn, dbm, idx, start=start)
    print(f"[done] 指数采集完成，共 {total} 行（{today}）")

    if args.backfill:
        return 0

    # Tushare 通道：确认今天是交易日再拉全市场个股
    with conn.cursor() as cur:
        cur.execute("SELECT is_open FROM trade_cal WHERE cal_date=%s", (today,))
        row = cur.fetchone()
    if row and row[0] == 0:
        print(f"[skip] {today} 非交易日")
        return 0

    try:
        n = tushare_client.fetch_daily(conn, dbm, today)
        print(f"[tushare] 全市场日线 {today}: +{n} 行")
    except Exception as e:
        print(f"[warn] 全市场日线采集失败: {e}")
    print("[done] 每日采集完成")
    return 0


if __name__ == "__main__":
    sys.exit(main())
