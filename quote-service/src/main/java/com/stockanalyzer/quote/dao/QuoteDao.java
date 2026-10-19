package com.stockanalyzer.quote.dao;

import com.stockanalyzer.quote.model.IndexDaily;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/** 行情数据访问（MySQL） */
@Repository
public class QuoteDao {

    private final JdbcTemplate jdbc;

    public QuoteDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** 按日期降序取某指数最近 N 个交易日 */
    public List<IndexDaily> findRecent(String tsCode, int limit) {
        return jdbc.query(
                "SELECT ts_code, trade_date, close, pct_chg FROM index_daily "
                        + "WHERE ts_code = ? ORDER BY trade_date DESC LIMIT ?",
                (rs, i) -> new IndexDaily(
                        rs.getString("ts_code"),
                        rs.getString("trade_date"),
                        rs.getDouble("close"),
                        rs.getDouble("pct_chg")),
                tsCode, limit);
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
