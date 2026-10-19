"""MySQL 数据访问层：建库、upsert、采集日志"""
import pymysql
from datetime import datetime

import config


def get_conn() -> pymysql.Connection:
    return pymysql.connect(
        host=config.MYSQL_HOST,
        port=config.MYSQL_PORT,
        user=config.MYSQL_USER,
        password=config.MYSQL_PASSWORD,
        database=config.MYSQL_DB,
        charset='utf8mb4',
        autocommit=False,
    )


def init_db(conn: pymysql.Connection) -> None:
    with open(config.SCHEMA_PATH, encoding="utf-8") as f:
        sql = f.read()
    with conn.cursor() as cur:
        # MySQL 不支持 executescript，按分号切分执行
        for stmt in sql.split(';'):
            stmt = stmt.strip()
            if stmt and not stmt.startswith('--'):
                cur.execute(stmt)
    conn.commit()


def upsert_many(conn: pymysql.Connection, table: str, columns: list[str], rows: list[tuple]) -> int:
    """INSERT ... ON DUPLICATE KEY UPDATE，依赖表主键去重，可反复回填"""
    if not rows:
        return 0
    placeholders = ",".join(["%s"] * len(columns))
    cols = ",".join(columns)
    updates = ",".join([f"{c}=VALUES({c})" for c in columns])
    sql = f"INSERT INTO {table} ({cols}) VALUES ({placeholders}) ON DUPLICATE KEY UPDATE {updates}"
    with conn.cursor() as cur:
        cur.executemany(sql, rows)
    conn.commit()
    return len(rows)


def log_collect(conn: pymysql.Connection, source: str, target: str,
                rows: int, status: str, message: str = "") -> None:
    with conn.cursor() as cur:
        cur.execute(
            "INSERT INTO collect_log (run_at, source, target, `rows`, status, message) VALUES (%s,%s,%s,%s,%s,%s)",
            (datetime.now().strftime("%Y-%m-%d %H:%M:%S"), source, target, rows, status, message[:500]),
        )
    conn.commit()


def last_trade_date(conn: pymysql.Connection, table: str, ts_code: str) -> str | None:
    """查某标的已入库的最新交易日，用于增量采集"""
    with conn.cursor() as cur:
        cur.execute(
            f"SELECT MAX(trade_date) FROM {table} WHERE ts_code=%s", (ts_code,)
        )
        row = cur.fetchone()
        return row[0].strftime("%Y-%m-%d") if row and row[0] else None
