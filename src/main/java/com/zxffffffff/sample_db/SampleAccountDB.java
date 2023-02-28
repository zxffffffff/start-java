/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.sample_tools.SampleTools;

import java.sql.*;

public class SampleAccountDB extends SampleMySQL {
    SampleAccountDB(String host, String user, String pwd) {
        super(host, user, pwd, "test_account");
    }

    /**
     * 注册（user_account_pwd）
     *
     * @param user_name    用户名
     * @param user_email   邮箱，空=""
     * @param user_phone   手机号
     * @param user_pwd_md5 密码-MD5
     * @return user_id
     */
    public long signup(String user_name, String user_email, String user_phone, String user_pwd_md5) {
        // [0] 检查参数
        if (!SampleTools.checkIsValidName(user_name)) {
            throw new RuntimeException("invalid user name");
        }
        if (user_email != null && !user_email.isEmpty() && !SampleTools.checkIsValidMail(user_email)) {
            throw new RuntimeException("invalid user email");
        }
        if (!SampleTools.checkIsValidPhone(user_phone)) {
            throw new RuntimeException("invalid user phone");
        }
        if (user_pwd_md5.length() != 32) {
            throw new RuntimeException("invalid password MD5");
        }

        // todo info

        // [1] 检查user是否存在
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT id FROM user_account_pwd WHERE user_name=? or user_email=? or user_phone=?;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                int index = 1; // 注意：索引从1开始
                pst.setString(index++, user_name);
                pst.setString(index++, user_email);
                pst.setString(index, user_phone);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        throw new RuntimeException("user name/email/phone exists");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // [2] 插入user
        long user_id;
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "INSERT INTO user_account_pwd (create_time,update_time,user_id,user_password,user_name,user_phone,user_email) VALUES (?,?,?,?,?,?,?);";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                user_id = SampleTools.generateSnowFlakeID();
                String user_password = SampleTools.getSaltPassword(user_pwd_md5);
                long dt = System.currentTimeMillis();

                int index = 1; // 注意：索引从1开始
                pst.setTimestamp(index++, new java.sql.Timestamp(dt));
                pst.setTimestamp(index++, new java.sql.Timestamp(dt));
                pst.setLong(index++, user_id);
                pst.setString(index++, user_password);
                pst.setString(index++, user_name);
                pst.setString(index++, user_phone);
                if (user_email == null || user_email.isEmpty())
                    pst.setNull(index, Types.VARCHAR);
                else
                    pst.setString(index, user_email);
                int ret = pst.executeUpdate();
                assert (ret == 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user_id;
    }

    /**
     * 登陆（user_account_pwd）
     *
     * @param user_name_email_phone 支持用户名、邮箱、手机号登陆
     * @param user_pwd_md5          密码-MD5
     * @return user_id
     */
    public long login(String user_name_email_phone, String user_pwd_md5) {
        // [0] 检查参数
        if (user_pwd_md5.length() != 32) {
            throw new RuntimeException("invalid password MD5");
        }

        // [1] 判断类型
        String sql = "SELECT user_id FROM user_account_pwd WHERE user_password=? AND ";
        if (SampleTools.checkIsValidMail(user_name_email_phone)) {
            sql += "user_email=?;";
        } else if (SampleTools.checkIsValidPhone(user_name_email_phone)) {
            sql += "user_phone=?;";
        } else if (SampleTools.checkIsValidName(user_name_email_phone)) {
            sql += "user_name=?;";
        } else {
            throw new RuntimeException("user name/email/phone err");
        }
        String user_password = SampleTools.getSaltPassword(user_pwd_md5);

        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                int index = 1; // 注意：索引从1开始
                pst.setString(index++, user_password);
                pst.setString(index, user_name_email_phone);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    } else {
                        throw new RuntimeException("user name or password err");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
