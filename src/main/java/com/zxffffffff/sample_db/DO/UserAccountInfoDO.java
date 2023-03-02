package com.zxffffffff.sample_db.DO;

/**
 * 用户基础信息（user_account_info）
 *
 * @param user_id  用户唯一id
 * @param nickname 用户昵称，初始随机生成
 * @param sex      用户性别，可以为0表示未填写
 * @param age      用户年龄，可以为-1表示未填写
 * @param industry 用户职业/行业，可以为""表示未填写
 */
public record UserAccountInfoDO(long user_id, String nickname, Sex sex, int age, String industry) {
    /**
     * 未填写=0，男=1，女=2
     */
    public enum Sex {
        Undefined, Male, Female
    }

    public UserAccountInfoDO {
        assert (nickname != null);
        assert (sex != null);
        assert (industry != null);
    }

    /**
     * 注册参数
     */
    static public UserAccountInfoDO forSignup(String nickname, Sex sex, int age, String industry) {
        return new UserAccountInfoDO(0, nickname, sex, age, industry);
    }
}
