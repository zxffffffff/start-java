/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.DO.ChatContactsAddDO;
import com.zxffffffff.DO.ChatContactsMessageDO;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天IM相关
 * 线程安全，可重入
 */
public class ChatContactDAO extends BaseMySQLClient {
    public ChatContactDAO(String host, String user, String pwd) {
        super(host, user, pwd, "test_chat");
    }

    /**
     * 查询联系人申请记录（chat_contacts_add）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     * @return 无申请记录=null
     */
    public ChatContactsAddDO getContactAdd(long user_id, long contact_user_id) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            // [1] 查询
            String sql = "SELECT add_status FROM chat_contacts_add WHERE user_id=? and contact_user_id=? ORDER BY id DESC limit 1;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int add_status = rs.getInt(1);
                        return new ChatContactsAddDO(user_id, contact_user_id, ChatContactsAddDO.AddStatus.values()[add_status]);
                    }
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询联系人申请列表（chat_contacts_add）
     *
     * @param user_id 用户
     * @return 联系人申请列表
     */
    public List<ChatContactsAddDO> getContactAddList(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            // [1] 查询
            String sql = "SELECT contact_user_id,add_status FROM chat_contacts_add WHERE user_id=? ORDER BY id DESC;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                try (ResultSet rs = pst.executeQuery()) {
                    List<ChatContactsAddDO> list = new ArrayList<>();
                    while (rs.next()) {
                        long contact_user_id = rs.getLong(1);
                        int add_status = rs.getInt(2);
                        list.add(new ChatContactsAddDO(user_id, contact_user_id, ChatContactsAddDO.AddStatus.values()[add_status]));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 申请联系人（chat_contacts_add）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     * @param add_msg         留言
     */
    public void addContactAdd(long user_id, long contact_user_id, String add_msg) {
        // [1] 检查是否已添加好友
        boolean is = isContact(user_id, contact_user_id);
        if (is) {
            throw new RuntimeException("already a contact");
        }

        // [2] 检查是否重复提交申请
        ChatContactsAddDO contactAdd = getContactAdd(user_id, contact_user_id);
        if (contactAdd != null && contactAdd.add_status() == ChatContactsAddDO.AddStatus.Waiting) {
            throw new RuntimeException("add task exist");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            // [3] 提交申请
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            String sql3 = "INSERT INTO chat_contacts_add (create_time,update_time,user_id,contact_user_id,add_msg,add_status) VALUES (?,?,?,?,?,?);";
            try (PreparedStatement pst = conn.prepareStatement(sql3)) {
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
     * 同意或拒绝联系人申请（chat_contacts_add）
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

        // [1] 检查是否申请中
        ChatContactsAddDO contactAdd = getContactAdd(user_id, contact_user_id);
        if (contactAdd == null || contactAdd.add_status() != ChatContactsAddDO.AddStatus.Waiting) {
            throw new RuntimeException("add task not exist");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            if (add_status == ChatContactsAddDO.AddStatus.Accepted) {
                // [2-1] 开启事务更新
                conn.setAutoCommit(false);
                try {
                    String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE user_id=? and contact_user_id=?;";
                    try (PreparedStatement pst = conn.prepareStatement(sql)) {
                        pst.setTimestamp(1, dt);
                        pst.setInt(2, add_status.ordinal());
                        pst.setLong(3, user_id);
                        pst.setLong(4, contact_user_id);

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
                    throw new RuntimeException("error rollback: " + e);
                } finally {
                    conn.setAutoCommit(true);
                }
            } else /* add_status == 3 */ {
                // [2-2] 更新
                String sql = "UPDATE chat_contacts_add SET update_time=?, add_status=? WHERE user_id=? and contact_user_id=?;";
                try (PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setTimestamp(1, dt);
                    pst.setInt(2, add_status.ordinal());
                    pst.setLong(3, user_id);
                    pst.setLong(4, contact_user_id);

                    int ret = pst.executeUpdate();
                    assert (ret == 1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查是否联系人（chat_contacts）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     */
    public boolean isContact(long user_id, long contact_user_id) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            // [1] 检查是否已添加好友
            String sql = "SELECT COUNT(*) FROM chat_contacts WHERE user_id=? and contact_user_id=?;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);

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
     * 查询联系人id列表（chat_contacts）
     *
     * @param user_id 用户
     * @return 联系人
     */
    public List<Long> getContactList(long user_id) {
        // [0] 检查参数
        if (user_id == 0) {
            throw new RuntimeException("invalid id");
        }

        // [1] 查询
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

    /**
     * 插入聊天信息（chat_contacts_message）
     *
     * @param messageDO 必须 user_id，contact_user_id，msg_type，msg_text/msg_file
     */
    public void setContactMessage(ChatContactsMessageDO messageDO) {
        // [1] 检查是否已添加好友
        boolean is = isContact(messageDO.user_id(), messageDO.contact_user_id());
        if (!is) {
            throw new RuntimeException("not a contact");
        }

        try (Connection conn = this.dataSource.getConnection()) {
            // [3] 提交
            Timestamp dt = new java.sql.Timestamp(System.currentTimeMillis());
            String sql3 = "INSERT INTO chat_contacts_message (create_time,update_time,user_id,contact_user_id,msg_type,msg_text,msg_file) VALUES (?,?,?,?,?,?,?);";
            try (PreparedStatement pst = conn.prepareStatement(sql3)) {
                pst.setTimestamp(1, dt);
                pst.setTimestamp(2, dt);
                pst.setLong(3, messageDO.user_id());
                pst.setLong(4, messageDO.contact_user_id());
                pst.setInt(5, messageDO.msgType().ordinal());
                pst.setString(6, messageDO.msg_text());
                pst.setBlob(7, new SerialBlob(messageDO.msg_file()));

                int ret = pst.executeUpdate();
                assert (ret == 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 查询聊天信息-单向（chat_contacts_message）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     */
    public List<ChatContactsMessageDO> getContactMessage(long user_id, long contact_user_id, int offset, int limit) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }
        if (limit > 100 || offset > 100000) {
            throw new RuntimeException("invalid param");
        }

        // [1] 查询
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT a.update_time,a.msg_type,a.msg_text,a.msg_file,a.is_recalled FROM chat_contacts_message as a, (SELECT id FROM chat_contacts_message WHERE user_id=? and contact_user_id=? ORDER BY id DESC LIMIT ?,?) as b WHERE a.id=b.id;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);
                pst.setInt(3, offset);
                pst.setInt(4, limit);

                try (ResultSet rs = pst.executeQuery()) {
                    List<ChatContactsMessageDO> list = new ArrayList<>();
                    while (rs.next()) {
                        Timestamp dt = rs.getTimestamp(1);
                        int msgType = rs.getInt(2);
                        String msgText = rs.getString(3);
                        Blob msgFile = rs.getBlob(4);
                        int is_recalled = rs.getInt(5);
                        list.add(new ChatContactsMessageDO(dt, user_id, contact_user_id, ChatContactsMessageDO.MsgType.values()[msgType], msgText, msgFile.getBytes(1, (int) msgFile.length()), (is_recalled == 1)));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 查询聊天信息-双向（chat_contacts_message）
     *
     * @param user_id         用户
     * @param contact_user_id 联系人
     */
    public List<ChatContactsMessageDO> getContactMessage2(long user_id, long contact_user_id, int offset, int limit) {
        // [0] 检查参数
        if (user_id == 0 || contact_user_id == 0) {
            throw new RuntimeException("invalid id");
        }
        if (limit > 100 || offset > 100000) {
            throw new RuntimeException("invalid param");
        }

        // [1] 查询
        try (Connection conn = this.dataSource.getConnection()) {
            String sql = "SELECT a.update_time,a.user_id,a.contact_user_id,a.msg_type,a.msg_text,a.msg_file,a.is_recalled FROM chat_contacts_message as a, (SELECT id FROM chat_contacts_message WHERE user_id=? and contact_user_id=? or contact_user_id=? and user_id=? ORDER BY id DESC LIMIT ?,?) as b WHERE a.id=b.id;";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setLong(1, user_id);
                pst.setLong(2, contact_user_id);
                pst.setLong(3, user_id);
                pst.setLong(4, contact_user_id);
                pst.setInt(5, offset);
                pst.setInt(6, limit);

                try (ResultSet rs = pst.executeQuery()) {
                    List<ChatContactsMessageDO> list = new ArrayList<>();
                    while (rs.next()) {
                        Timestamp dt = rs.getTimestamp(1);
                        long temp_user_id = rs.getLong(2);
                        long temp_contact_user_id = rs.getLong(3);
                        int msgType = rs.getInt(4);
                        String msgText = rs.getString(5);
                        Blob msgFile = rs.getBlob(6);
                        int is_recalled = rs.getInt(7);
                        list.add(new ChatContactsMessageDO(dt, temp_user_id, temp_contact_user_id, ChatContactsMessageDO.MsgType.values()[msgType], msgText, msgFile.getBytes(1, (int) msgFile.length()), (is_recalled == 1)));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
