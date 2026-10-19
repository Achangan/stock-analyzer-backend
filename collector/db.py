"""SQLite 数据访问层：建库、upsert、采集日志"""
import os
import sqlite3
from datetime import datetime

import config


def get_conn() -> sqlite3.Connection:
    os.makedirs(os.path.dirname(config.DB_PATH), exist_ok=True)
    conn = sqlite3.connect(config.DB_PATH)
    conn.execute("PRAGMA journal_mode=WAL")
    return conn


def init_db(conn: sqlite3.Connection) -> None:
    with open(config.SCHEMA_PATH, encoding="utf-8") as f:
        conn.executescript(f.read())
    conn.commit()


def upsert_many(conn: sqlite3.Connection, table: str, columns: list[str], rows: list[tuple]) -> int:
    """INSERT OR REPLACE，依赖表主键去重，可反复回填"""
    if not rows:
        return 0
    placeholders = ",".join(["?"] * len(columns))
    sql = f"INSERT OR REPLACE INTO {table} ({','.join(columns)}) VALUES ({placeholders})"
    conn.executemany(sql, rows)
    conn.commit()
    return len(rows)


def log_collect(conn: sqlite3.Connection, source: str, target: str,
                rows: int, status: str, message: str = "") -> None:
    conn.execute(
        "INSERT INTO collect_log (run_at, source, target, rows, status, message) VALUES (?,?,?,?,?,?)",
        (datetime.now().isoformat(timespec="seconds"), source, target, rows, status, message[:500]),
    )
    conn.commit()


def last_trade_date(conn: sqlite3.Connection, table: str, ts_code: str) -> str | None:
    """查某标的已入库的最新交易日，用于增量采集"""
    row = conn.execute(
        f"SELECT MAX(trade_date) FROM {table} WHERE ts_code=?", (ts_code,)
    ).fetchone()
    return row[0] if row and row[0] else None
