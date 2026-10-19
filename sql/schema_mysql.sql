-- stock-analyzer P1 数据库结构（MySQL 8.0）
-- 设计原则：原始数据原样落库，业务计算在上层做；主键防重，可反复回填

-- 交易日历
CREATE TABLE IF NOT EXISTS trade_cal (
    cal_date    DATE PRIMARY KEY,           -- YYYY-MM-DD
    is_open     TINYINT NOT NULL            -- 1=交易日 0=休市
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 个股日线行情
CREATE TABLE IF NOT EXISTS daily_quote (
    ts_code     VARCHAR(20) NOT NULL,       -- 600519.SH
    trade_date  DATE NOT NULL,              -- YYYY-MM-DD
    open        DECIMAL(12,4),
    high        DECIMAL(12,4),
    low         DECIMAL(12,4),
    close       DECIMAL(12,4),
    pre_close   DECIMAL(12,4),
    `change`    DECIMAL(12,4),
    pct_chg     DECIMAL(10,4),
    vol         DECIMAL(20,2),              -- 成交量（手）
    amount      DECIMAL(20,4),              -- 成交额（千元）
    PRIMARY KEY (ts_code, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_daily_date ON daily_quote(trade_date);

-- 指数日线行情（沪深300/中证500/创业板指等）
CREATE TABLE IF NOT EXISTS index_daily (
    ts_code     VARCHAR(20) NOT NULL,       -- 000300.SH
    trade_date  DATE NOT NULL,
    open        DECIMAL(12,4),
    high        DECIMAL(12,4),
    low         DECIMAL(12,4),
    close       DECIMAL(12,4),
    pre_close   DECIMAL(12,4),
    pct_chg     DECIMAL(10,4),
    vol         DECIMAL(20,2),
    amount      DECIMAL(20,4),
    PRIMARY KEY (ts_code, trade_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE INDEX idx_index_date ON index_daily(trade_date);

-- 采集任务日志（每次采集记录，便于排查）
CREATE TABLE IF NOT EXISTS collect_log (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    run_at      DATETIME NOT NULL,          -- ISO 时间
    source      VARCHAR(20) NOT NULL,       -- tushare / baostock
    target      VARCHAR(100) NOT NULL,      -- 表名或范围
    `rows`      INT,
    status      VARCHAR(20) NOT NULL,       -- success / failed
    message     TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
