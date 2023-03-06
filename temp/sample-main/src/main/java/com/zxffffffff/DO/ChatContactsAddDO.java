/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.DO;

/**
 * 添加联系人列表（chat_contacts_add）
 *
 * @param user_id         用户
 * @param contact_user_id 添加联系人
 * @param add_status      申请状态
 */
public record ChatContactsAddDO(long user_id, long contact_user_id, AddStatus add_status) {
    /**
     * 1=申请中，2=同意，3=拒绝
     */
    public enum AddStatus {
        Undefined, Waiting, Accepted, Refused
    }

    public ChatContactsAddDO {
        assert (add_status != null);
    }
}
