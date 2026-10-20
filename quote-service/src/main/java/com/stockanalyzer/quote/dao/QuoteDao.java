package com.stockanalyzer.quote.dao;

import com.stockanalyzer.quote.model.IndexDaily;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/** 行情数据访问（MySQL） */
@Repository
public class QuoteDao {

    private static final String COLUMNS =
            "ts_code, trade_date, open, high, low, close, pre_close, pct_chg, vol, amount";

    private final JdbcTemplate jdbc;

    public QuoteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 按日期降序取某指数最近 N 个交易日 */
    public List<IndexDaily> findRecent(String tsCode, int limit) {
        return jdbc.query(
                "SELECT " + COLUMNS + " FROM index_daily "
                        + "WHERE ts_code = ? ORDER BY trade_date DESC LIMIT ?",
                QuoteDao::mapRow,
                tsCode, limit);
    }

    /** 按日期升序取某指数 [start, end] 区间内的日线；start/end 为空则不限该端 */
    public List<IndexDaily> findRange(String tsCode, String start, String end) {
        StringBuilder sql = new StringBuilder(
                "SELECT " + COLUMNS + " FROM index_daily WHERE ts_code = ?");
        java.util.List<Object> args = new java.util.ArrayList<>();
        args.add(tsCode);
        if (start != null && !start.isEmpty()) {
            sql.append(" AND trade_date >= ?");
            args.add(start);
        }
        if (end != null && !end.isEmpty()) {
            sql.append(" AND trade_date <= ?");
            args.add(end);
        }
        sql.append(" ORDER BY trade_date ASC");
        return jdbc.query(sql.toString(), QuoteDao::mapRow, args.toArray());
    }

    /** 已入库的指数列表（去重） */
    public List<String> findIndexCodes() {
        return jdbc.queryForList(
                "SELECT DISTINCT ts_code FROM index_daily ORDER BY ts_code", String.class);
    }

    /** 数据完整性：每个指数的起止日期与行数 */
    public List<IndexCoverage> findCoverage() {
        return jdbc.query(
                "SELECT ts_code, MIN(trade_date) AS start_date, MAX(trade_date) AS end_date, "
                        + "COUNT(*) AS rows_count FROM index_daily GROUP BY ts_code ORDER BY ts_code",
                (rs, i) -> new IndexCoverage(
                        rs.getString("ts_code"),
                        rs.getString("start_date"),
                        rs.getString("end_date"),
                        rs.getInt("rows_count")));
    }

    private static IndexDaily mapRow(ResultSet rs, int i) throws SQLException {
        return new IndexDaily(
                rs.getString("ts_code"),
                rs.getString("trade_date"),
                dbl(rs, "open"),
                dbl(rs, "high"),
                dbl(rs, "low"),
                dbl(rs, "close"),
                dbl(rs, "pre_close"),
                dbl(rs, "pct_chg"),
                dbl(rs, "vol"),
                dbl(rs, "amount"));
    }

    /** 允许为 NULL 的数值读取 */
    private static Double dbl(ResultSet rs, String column) throws SQLException {
        double v = rs.getDouble(column);
        return rs.wasNull() ? null : v;
    }

    public static class IndexCoverage {
        public final String tsCode;
        public final String startDate;
        public final String endDate;
        public final int rows;

        public IndexCoverage(String tsCode, String startDate, String endDate, int rows) {
            this.tsCode = tsCode;
            this.startDate = startDate;
            this.endDate = endDate;
            this.rows = rows;
        }
    }
}
