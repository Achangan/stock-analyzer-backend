"""SQLite → MySQL 数据迁移脚本"""
import sqlite3
import pymysql
import os

BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SQLITE_PATH = os.path.join(BASE_DIR, "data", "stock.db")

MYSQL_HOST = "localhost"
MYSQL_PORT = 3306
MYSQL_USER = "root"
MYSQL_PASSWORD = "root123456"
MYSQL_DB = "stock_analyzer"


def migrate():
    # 连接 SQLite
    sqlite_conn = sqlite3.connect(SQLITE_PATH)
    sqlite_conn.row_factory = sqlite3.Row

    # 连接 MySQL
    mysql_conn = pymysql.connect(
        host=MYSQL_HOST, port=MYSQL_PORT,
        user=MYSQL_USER, password=MYSQL_PASSWORD,
        database=MYSQL_DB, charset='utf8mb4'
    )

    # 1. 迁移 index_daily
    print("迁移 index_daily...")
    rows = sqlite_conn.execute(
        "SELECT ts_code, trade_date, open, high, low, close, pre_close, pct_chg, vol, amount FROM index_daily"
    ).fetchall()
    with mysql_conn.cursor() as cur:
        for r in rows:
            cur.execute("""
                INSERT INTO index_daily (ts_code, trade_date, open, high, low, close, pre_close, pct_chg, vol, amount)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
                ON DUPLICATE KEY UPDATE
                open=VALUES(open), high=VALUES(high), low=VALUES(low), close=VALUES(close),
                pre_close=VALUES(pre_close), pct_chg=VALUES(pct_chg), vol=VALUES(vol), amount=VALUES(amount)
            """, (r['ts_code'], r['trade_date'], r['open'], r['high'], r['low'], r['close'],
                  r['pre_close'], r['pct_chg'], r['vol'], r['amount']))
    mysql_conn.commit()
    print(f"  index_daily: {len(rows)} 行")

    # 2. 迁移 collect_log
    print("迁移 collect_log...")
    rows = sqlite_conn.execute(
        "SELECT run_at, source, target, rows, status, message FROM collect_log"
    ).fetchall()
    with mysql_conn.cursor() as cur:
        for r in rows:
            cur.execute("""
                INSERT INTO collect_log (run_at, source, target, `rows`, status, message)
                VALUES (%s, %s, %s, %s, %s, %s)
            """, (r['run_at'], r['source'], r['target'], r['rows'], r['status'], r['message']))
    mysql_conn.commit()
    print(f"  collect_log: {len(rows)} 行")

    # 3. 验证
    print("\n验证...")
    with mysql_conn.cursor() as cur:
        cur.execute("SELECT COUNT(*) FROM index_daily")
        n = cur.fetchone()[0]
        print(f"  MySQL index_daily: {n} 行")
        cur.execute("SELECT COUNT(*) FROM collect_log")
        n = cur.fetchone()[0]
        print(f"  MySQL collect_log: {n} 行")

    sqlite_conn.close()
    mysql_conn.close()
    print("\n迁移完成！")


if __name__ == "__main__":
    migrate()
