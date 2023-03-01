/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.sample_db.DO.ChatContactsAddDO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatContactService extends BaseMySQLService {
    ChatContactService(String host, String user, String pwd) {
        super(host, user, pwd, "test_chat");
    }

    /**
     * 添加联系人（chat_contacts_add）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     * @param add_msg         留言
     */
    public void addContactAdd(long user_id, long contact_user_id, String add_msg) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        // [1] 检查是否已存在
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT id FROM chat_contacts_add WHERE user_id=? and contact_user_id=? and add_status='1';";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        throw new RuntimeException("user add task exists");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // [2] 插入
        try (Connection conn = this.dataSource.getConnection()) {
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            String sql = "INSERT INTO chat_contacts_add (create_time,update_time,user_id,contact_user_id,add_msg,add_status) VALUES (?,?,?,?,?,?);";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setTimestamp(1, dt);
                pst.setTimestamp(2, dt);
                pst.setLong(3, user_id);
                pst.setLong(4, contact_user_id);
                pst.setString(5, add_msg);
                pst.setInt(6, 1);
                int ret = pst.executeUpdate();
                assert (ret == 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取添加联系人列表（chat_contacts_add）
     *
     * @param user_id 用户
     * @return id, status
     */
    public List<ChatContactsAddDO> getContactAddList(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT contact_user_id,add_status FROM chat_contacts_add WHERE user_id=? ORDER BY id DESC;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    List<ChatContactsAddDO> list = new ArrayList<>();
                    while (rs.next()) {
                        long contact_user_id = rs.getLong(1);
                        int add_status = rs.getInt(2);
                        list.add(new ChatContactsAddDO(contact_user_id, ChatContactsAddDO.AddStatus.values()[add_status]));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 同意或拒绝添加联系人（chat_contacts_add，chat_contacts）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     * @param add_status      状态
     */
    public void setContactAddStatus(long user_id, long contact_user_id, ChatContactsAddDO.AddStatus add_status) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }
        if (add_status != ChatContactsAddDO.AddStatus.Accepted && add_status != ChatContactsAddDO.AddStatus.Refused) {
            throw new RuntimeException("invalid status");
        }

        // [1] 检查是否已存在
        long id;
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT id FROM chat_contacts_add WHERE user_id=? and contact_user_id=? and add_status='1';";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        id = rs.getLong(1);
                    } else {
                        throw new RuntimeException("user add task not exists");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // [2] 更新
        try (Connection conn = this.dataSource.getConnection()) {
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            if (add_status == ChatContactsAddDO.AddStatus.Accepted) {
                // 开启事务
                conn.setAutoCommit(false);
                try {
                    String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE (id=?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql)) {
                        pst.setTimestamp(1, dt);
                        pst.setInt(2, add_status.ordinal());
                        pst.setLong(3, id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    String sql2 = "INSERT INTO chat_contacts (create_time,update_time,user_id,contact_user_id) VALUES (?,?,?,?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql2)) {
                        pst.setTimestamp(1, dt);
                        pst.setTimestamp(2, dt);
                        pst.setLong(3, user_id);
                        pst.setLong(4, contact_user_id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    String sql3 = "INSERT INTO chat_contacts (create_time,update_time,user_id,contact_user_id) VALUES (?,?,?,?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql3)) {
                        pst.setTimestamp(1, dt);
                        pst.setTimestamp(2, dt);
                        pst.setLong(3, contact_user_id);
                        pst.setLong(4, user_id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("error rollback");
                } finally {
                    conn.setAutoCommit(true);
                }
            } else /* add_status == 3 */ {
                String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE (id=?);";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setTimestamp(1, dt);
                    pst.setInt(2, add_status.ordinal());
                    pst.setLong(3, id);
                    int ret = pst.executeUpdate();
                    assert (ret == 1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 联系人列表（chat_contacts，user_account_info）
     *
     * @param user_id 用户
     * @return 联系人
     */
    public List<Long> getContactList(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT contact_user_id,contact_user_privacy FROM chat_contacts WHERE user_id=? ORDER BY id DESC;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    List<Long> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(rs.getLong(1));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
