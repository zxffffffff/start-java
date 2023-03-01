package com.zxffffffff.sample_db.DO;

/**
 * 添加联系人列表（chat_contacts_add）
 *
 * @param contact_user_id 添加联系人
 * @param add_status      申请状态
 */
public record ChatContactsAddDO(long contact_user_id, AddStatus add_status) {
    /**
     * 1=申请中，2=同意，3=拒绝
     */
    public enum AddStatus {
        Undefined, Waiting, Accepted, Refused
    }

    public ChatContactsAddDO() {
        this(0, AddStatus.Undefined);
    }
}
