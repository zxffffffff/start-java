package com.zxffffffff.sample_db;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SampleChatDB extends SampleMySQL {
    /**
     * 1=申请中，2=同意，3=拒绝
     */
    enum EnContactAddStatus {
        Undefined, Waiting, Accepted, Refused
    }

    SampleChatDB(String host, String user, String pwd) {
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
                int index = 1; // 注意：索引从1开始
                pst.setLong(index++, user_id);
                pst.setLong(index, contact_user_id);
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
            String sql = "INSERT INTO chat_contacts_add (create_time,update_time,user_id,contact_user_id,add_msg,add_status) VALUES (?,?,?,?,?,?);";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                java.util.Date date = new Date(System.currentTimeMillis());

                int index = 1; // 注意：索引从1开始
                pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                pst.setLong(index++, user_id);
                pst.setLong(index++, contact_user_id);
                pst.setString(index++, add_msg);
                pst.setInt(index, 1);
                int ret = pst.executeUpdate();
                assert (ret == 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 添加联系人列表（chat_contacts_add）
     *
     * @param user_id 用户
     * @return id, status
     */
    public List<Pair<Long, EnContactAddStatus>> getContactAddList(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        List<Pair<Long, EnContactAddStatus>> list = new ArrayList<>();
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT contact_user_id,add_status FROM chat_contacts_add WHERE user_id=? ORDER BY id DESC;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                int index = 1; // 注意：索引从1开始
                pst.setLong(index, user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    while (rs.next()) {
                        long contact_user_id = rs.getLong(1);
                        int add_status = rs.getInt(2);
                        list.add(new ImmutablePair<>(contact_user_id, EnContactAddStatus.values()[add_status]));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    /**
     * 同意或拒绝添加联系人（chat_contacts_add，chat_contacts）
     * 警告：status=2同意时，会开启事物多表插入
     *
     * @param user_id              用户
     * @param contact_user_id      联系人
     * @param add_status           状态
     */
    public void setContactAddStatus(long user_id, long contact_user_id, EnContactAddStatus add_status) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }
        if (add_status != EnContactAddStatus.Accepted && add_status != EnContactAddStatus.Refused) {
            throw new RuntimeException("invalid status");
        }

        // [1] 检查是否已存在
        long id;
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT id FROM chat_contacts_add WHERE user_id=? and contact_user_id=? and add_status='1';";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                int index = 1; // 注意：索引从1开始
                pst.setLong(index++, user_id);
                pst.setLong(index, contact_user_id);
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
            java.util.Date date = new Date(System.currentTimeMillis());
            if (add_status == EnContactAddStatus.Accepted) {
                // 开启事物多表插入
                conn.setAutoCommit(false);
                try {
                    String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE (id=?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql)) {
                        int index = 1; // 注意：索引从1开始
                        pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                        pst.setInt(index++, add_status.ordinal());
                        pst.setLong(index, id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    String sql2 = "INSERT INTO chat_contacts (create_time,update_time,user_id,contact_user_id) VALUES (?,?,?,?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql2)) {
                        int index = 1; // 注意：索引从1开始
                        pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                        pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                        pst.setLong(index++, user_id);
                        pst.setLong(index, contact_user_id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    String sql3 = "INSERT INTO chat_contacts (create_time,update_time,user_id,contact_user_id) VALUES (?,?,?,?);";
                    try (PreparedStatement pst = conn.prepareStatement(sql3)) {
                        int index = 1; // 注意：索引从1开始
                        pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                        pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                        pst.setLong(index++, contact_user_id);
                        pst.setLong(index, user_id);
                        int ret = pst.executeUpdate();
                        assert (ret == 1);
                    }

                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException(e);
                } finally {
                    conn.setAutoCommit(true);
                }
            } else /* add_status == 3 */ {
                String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE (id=?);";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    int index = 1; // 注意：索引从1开始
                    pst.setTimestamp(index++, new java.sql.Timestamp(date.getTime()));
                    pst.setInt(index++, add_status.ordinal());
                    pst.setLong(index, id);
                    int ret = pst.executeUpdate();
                    assert (ret == 1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
