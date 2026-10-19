-- stock-analyzer P1 数据库结构（SQLite）
-- 设计原则：原始数据原样落库，业务计算在上层做；主键防重，可反复回填

-- 交易日历
CREATE TABLE IF NOT EXISTS trade_cal (
    cal_date    TEXT PRIMARY KEY,   -- YYYY-MM-DD
    is_open     INTEGER NOT NULL    -- 1=交易日 0=休市
);

-- 个股日线行情
CREATE TABLE IF NOT EXISTS daily_quote (
    ts_code     TEXT NOT NULL,      -- 600519.SH
    trade_date  TEXT NOT NULL,      -- YYYY-MM-DD
    open        REAL,
    high        REAL,
    low         REAL,
    close       REAL,
    pre_close   REAL,
    change      REAL,
    pct_chg     REAL,
    vol         REAL,               -- 成交量（手）
    amount      REAL,               -- 成交额（千元）
    PRIMARY KEY (ts_code, trade_date)
);
CREATE INDEX IF NOT EXISTS idx_daily_date ON daily_quote(trade_date);

-- 指数日线行情（沪深300/中证500/创业板指等）
CREATE TABLE IF NOT EXISTS index_daily (
    ts_code     TEXT NOT NULL,      -- 000300.SH
    trade_date  TEXT NOT NULL,
    open        REAL,
    high        REAL,
    low         REAL,
    close       REAL,
    pre_close   REAL,
    pct_chg     REAL,
    vol         REAL,
    amount      REAL,
    PRIMARY KEY (ts_code, trade_date)
);
CREATE INDEX IF NOT EXISTS idx_index_date ON index_daily(trade_date);

-- 采集任务日志（每次采集记录，便于排查）
CREATE TABLE IF NOT EXISTS collect_log (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    run_at      TEXT NOT NULL,      -- ISO 时间
    source      TEXT NOT NULL,      -- tushare / baostock
    target      TEXT NOT NULL,      -- 表名或范围
    rows        INTEGER,
    status      TEXT NOT NULL,      -- success / failed
    message     TEXT
);
