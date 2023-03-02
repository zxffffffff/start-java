/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.sample_db.DO.UserAccountInfoDO;
import com.zxffffffff.sample_db.DO.UserAccountPwdDO;
import com.zxffffffff.sample_tools.BaseTools;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAccountService extends BaseMySQLService {
    UserAccountService(String host, String user, String pwd) {
        super(host, user, pwd, "test_account");
    }

    /**
     * 注册前检查user是否存在（user_account_pwd）
     *
     * @param pwdDO 必须 user_name,user_email,user_phone
     */
    public boolean exists(UserAccountPwdDO pwdDO) {
        // [0] 检查参数
        if (!BaseTools.checkIsValidName(pwdDO.user_name())) {
            throw new RuntimeException("invalid user name");
        }
        if (!pwdDO.user_email().isEmpty() && !BaseTools.checkIsValidMail(pwdDO.user_email())) {
            throw new RuntimeException("invalid user email");
        }
        if (!BaseTools.checkIsValidPhone(pwdDO.user_phone())) {
            throw new RuntimeException("invalid user phone");
        }

        // [1] select
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT COUNT(*) FROM user_account_pwd WHERE user_name=? or user_email=? or user_phone=?;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, pwdDO.user_name());
                pst.setString(2, pwdDO.user_email());
                pst.setString(3, pwdDO.user_phone());

                try (ResultSet rs = pst.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 注册（user_account_pwd，user_account_info）
     *
     * @param pwdDO  必须 user_name,user_email,user_phone,user_pwd_md5
     * @param infoDO 必须 nickname,sex,age,industry
     * @return user_id
     */
    public long signup(UserAccountPwdDO pwdDO, UserAccountInfoDO infoDO) {
        // [0] 检查user是否存在
        var exist = exists(pwdDO);
        if (exist) {
            throw new RuntimeException("user name/email/phone exists");
        }

        // [1] 检查参数（checkSignupAccount 已完成部分参数检查）
        if (pwdDO.user_pwd_md5().length() != 32) {
            throw new RuntimeException("invalid password MD5");
        }

        // [2] 插入user
        long user_id = BaseTools.generateSnowFlakeID();
        try (Connection conn = this.dataSource.getConnection()) {
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            // 开启事务
            conn.setAutoCommit(false);
            try {
                String sql = "INSERT INTO user_account_pwd (create_time,update_time,user_id,user_password,user_name,user_phone,user_email) VALUES (?,?,?,?,?,?,?);";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    String user_password = BaseTools.getSaltPassword(pwdDO.user_pwd_md5());

                    pst.setTimestamp(1, dt);
                    pst.setTimestamp(2, dt);
                    pst.setLong(3, user_id);
                    pst.setString(4, user_password);
                    pst.setString(5, pwdDO.user_name());
                    pst.setString(6, pwdDO.user_phone());
                    if (pwdDO.user_email().isEmpty()) pst.setNull(7, Types.VARCHAR);
                    else pst.setString(7, pwdDO.user_email());

                    int ret = pst.executeUpdate();
                    assert (ret == 1);
                }

                String sql2 = "INSERT INTO user_account_info (create_time,update_time,user_id,nickname,sex,age,industry,id_card) VALUES (?,?,?,?,?,?,?,?);";
                try (PreparedStatement pst = conn.prepareStatement(sql2)) {
                    pst.setTimestamp(1, dt);
                    pst.setTimestamp(2, dt);
                    pst.setLong(3, user_id);
                    pst.setString(4, infoDO.nickname());
                    pst.setInt(5, infoDO.sex().ordinal());
                    pst.setInt(6, infoDO.age());
                    pst.setString(7, infoDO.industry());
                    pst.setString(8, "");

                    int ret = pst.executeUpdate();
                    assert (ret == 1);
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("error rollback: " + e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return user_id;
    }

    /**
     * 登陆（user_account_pwd，user_account_info）
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
        if (BaseTools.checkIsValidMail(user_name_email_phone)) {
            sql += "user_email=?;";
        } else if (BaseTools.checkIsValidPhone(user_name_email_phone)) {
            sql += "user_phone=?;";
        } else if (BaseTools.checkIsValidName(user_name_email_phone)) {
            sql += "user_name=?;";
        } else {
            throw new RuntimeException("user name/email/phone err");
        }
        String user_password = BaseTools.getSaltPassword(user_pwd_md5);

        try (Connection conn = this.dataSource.getConnection()) {
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, user_password);
                pst.setString(2, user_name_email_phone);

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

    /**
     * 用户信息（user_account_info）
     *
     * @param user_id 用户id
     * @return 用户信息
     */
    public UserAccountInfoDO getInfo(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid user_id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT nickname,sex,age,industry FROM user_account_info where user_id=?;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        String nickname = rs.getString(1);
                        int sex = rs.getInt(2);
                        int age = rs.getInt(3);
                        String industry = rs.getString(4);
                        return new UserAccountInfoDO(user_id, nickname, UserAccountInfoDO.Sex.values()[sex], age, industry);
                    } else {
                        throw new RuntimeException("user id err");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 用户信息批量查询（user_account_info）
     *
     * @param list_user_id 用户id
     * @return 用户信息
     */
    public List<UserAccountInfoDO> getInfoList(List<Long> list_user_id) {
        // [0] 检查参数
        if (list_user_id.isEmpty()) {
            return new ArrayList<>();
        }
        for (long user_id : list_user_id) {
            if (user_id == 0) {
                throw new RuntimeException("invalid user_id");
            }
        }

        try (Connection conn = this.dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT user_id,nickname,sex,age,industry FROM user_account_info where");
            for (int i = 0; i < list_user_id.size(); ++i) {
                if (i > 0) sql.append(" or");
                sql.append(" user_id=?");
            }
            sql.append(" ORDER BY id DESC;");
            try (PreparedStatement pst = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < list_user_id.size(); ++i) {
                    pst.setLong(i + 1, list_user_id.get(i));
                }

                try (ResultSet rs = pst.executeQuery()) {
                    List<UserAccountInfoDO> ret = new ArrayList<>();
                    while (rs.next()) {
                        long user_id = rs.getLong(1);
                        String nickname = rs.getString(2);
                        int sex = rs.getInt(3);
                        int age = rs.getInt(4);
                        String industry = rs.getString(5);
                        ret.add(new UserAccountInfoDO(user_id, nickname, UserAccountInfoDO.Sex.values()[sex], age, industry));
                    }
                    return ret;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
