package com.works.JessHsu.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

@Service
public class SequenceService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 啟動時自動檢查資料表是否存在，
     * 若不存在則建立 portfolio_serial_seq 並插入初始值。
     */
    @PostConstruct
    public void initSequenceTable() {
        // 建表（如果不存在）
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS portfolio_serial_seq (
                id INT PRIMARY KEY DEFAULT 1,
                current_value INT NOT NULL
            )
        """);

        // 確保至少有一筆資料
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM portfolio_serial_seq WHERE id = 1", Integer.class);

        if (count == null || count == 0) {
            jdbcTemplate.update("INSERT INTO portfolio_serial_seq (id, current_value) VALUES (1, 1000)");
            System.out.println("[INIT] portfolio_serial_seq created with starting value 1000");
        }
    }

    /**
     * 取得下一個作品流水號，例如：
     * W01001、W01002、W01003 ...
     */
    @Transactional
    public synchronized String nextPortfolioSerial() {
        // 更新流水號
        int updated = jdbcTemplate.update("UPDATE portfolio_serial_seq SET current_value = current_value + 1 WHERE id = 1");
        if (updated == 0) {
            throw new IllegalStateException("Failed to update portfolio_serial_seq, record missing!");
        }

        // 取得最新值
        Integer value = jdbcTemplate.queryForObject(
                "SELECT current_value FROM portfolio_serial_seq WHERE id = 1", Integer.class);

        if (value == null) {
            throw new IllegalStateException("Sequence table is empty or uninitialized");
        }

        // 回傳格式 W00001
        return String.format("W%05d", value);
    }
}