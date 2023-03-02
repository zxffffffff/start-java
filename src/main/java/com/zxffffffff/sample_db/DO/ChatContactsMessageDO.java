package com.zxffffffff.sample_db.DO;

import java.sql.Timestamp;

/**
 * @param update_time     发送时间
 * @param user_id         用户
 * @param contact_user_id 联系人
 * @param msgType         消息类型
 * @param msg_text        文字/文件名/链接
 * @param msg_file        文件
 * @param is_recalled     消息撤回
 */
public record ChatContactsMessageDO(Timestamp update_time, long user_id, long contact_user_id, MsgType msgType,
                                    String msg_text, byte[] msg_file, boolean is_recalled) {
    /**
     * 消息类型 0=text, 1=file(text=filename), 2=link(text)
     */
    public enum MsgType {
        Text, File, Link
    }

    public ChatContactsMessageDO {
        assert (msgType != null);
        assert (msg_text != null);
        assert (msg_file != null);
    }

    /**
     * 发送参数
     */
    static public ChatContactsMessageDO forSetText(long user_id, long contact_user_id, String msg_text) {
        return new ChatContactsMessageDO(new Timestamp(0), user_id, contact_user_id, MsgType.Text, msg_text, new byte[]{}, false);
    }

    /**
     * 发送参数
     */
    static public ChatContactsMessageDO forSetFile(long user_id, long contact_user_id, String msg_text, byte[] msg_file) {
        return new ChatContactsMessageDO(new Timestamp(0), user_id, contact_user_id, MsgType.File, msg_text, msg_file, false);
    }

    /**
     * 发送参数
     */
    static public ChatContactsMessageDO forSetLink(long user_id, long contact_user_id, String msg_text) {
        return new ChatContactsMessageDO(new Timestamp(0), user_id, contact_user_id, MsgType.Link, msg_text, new byte[]{}, false);
    }
}
