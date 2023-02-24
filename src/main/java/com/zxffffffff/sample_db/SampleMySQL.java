package com.zxffffffff.sample_db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.*;

public class SampleMySQL {
    DataSource dataSource = null;
    private String host;
    private String user;
    private String pwd;
    private String db;

    /**
     * 初始化 MySQL 连接池
     * @param host 地址 "localhost"
     * @param user 账号 "root"
     * @param pwd 密码 "123456"
     * @param db 数据库名 "test_db"
     */
    public SampleMySQL(String host, String user, String pwd, String db) {
        this.host = host;
        this.user = user;
        this.pwd = pwd;
        this.db = db;

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":3306/" + db);
        config.setUsername(user);
        config.setPassword(pwd);
        config.addDataSourceProperty("connectionTimeout", "1000"); // 连接超时：1秒
        config.addDataSourceProperty("idleTimeout", "60000"); // 空闲超时：60秒
        config.addDataSourceProperty("maximumPoolSize", "10"); // 最大连接数：10
        this.dataSource = new HikariDataSource(config);
    }

    public ResultSet runSql(String sql) {
        try (Connection conn = this.dataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery(sql)){
                    return rs;
                }
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /** 注册
     * @param use_name
     * @param use_email
     * @param use_phone
     * @param use_pwd
     * @return
     */
    public Boolean signup(String use_name, String use_email, String use_phone, String use_pwd) {
        return false;
    }

    /** 登陆
     * @param use_name_email_phone
     * @param use_pwd
     * @return
     */
    public Boolean login(String use_name_email_phone, String use_pwd) {
        return false;
    }
}
