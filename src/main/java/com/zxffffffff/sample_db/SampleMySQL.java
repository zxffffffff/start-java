package com.zxffffffff.sample_db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;

public class SampleMySQL {
    protected final DataSource dataSource;

    /**
     * 初始化 MySQL 连接池
     *
     * @param host 地址 "localhost"
     * @param user 账号 "root"
     * @param pwd  密码 "123456"
     * @param db   数据库名 "test_db"
     */
    public SampleMySQL(String host, String user, String pwd, String db) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":3306/" + db);
        config.setUsername(user);
        config.setPassword(pwd);
        config.addDataSourceProperty("connectionTimeout", "1000"); // 连接超时：1秒
        config.addDataSourceProperty("idleTimeout", "60000"); // 空闲超时：60秒
        config.addDataSourceProperty("maximumPoolSize", "10"); // 最大连接数：10
        this.dataSource = new HikariDataSource(config);
    }

    /**
     * 清空表，用于测试
     * drop     DDL 删表
     * truncate DDL 清空数据，自增归零
     * delete   DML 删数据，配合where使用，可回滚
     *
     * @param table 表名
     */
    public void truncateTable(String table) {
        String sql = String.format("TRUNCATE %s;", table);
        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
