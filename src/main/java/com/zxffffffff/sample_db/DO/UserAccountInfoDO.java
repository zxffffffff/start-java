package com.zxffffffff.sample_db.DO;

/**
 * 用户基础信息（user_account_info）
 *
 * @param user_id  用户唯一id
 * @param nickname 用户昵称，初始随机生成
 * @param sex      用户性别，可以为0对应数据库为NULL
 * @param age      用户年龄，可以为-1对应数据库为NULL
 * @param industry 用户职业/行业，可以为""对应数据库为NULL
 */
public record UserAccountInfoDO(long user_id, String nickname, Sex sex, int age, String industry) {
    /**
     * 男=1，女=2
     */
    public enum Sex {
        Undefined, Male, Female
    }

    public UserAccountInfoDO() {
        this(0, "", Sex.Undefined, -1, "");
    }

    /**
     * 注册参数
     */
    static public UserAccountInfoDO forSignup(String nickname) {
        return new UserAccountInfoDO(0, nickname, Sex.Undefined, -1, "");
    }

    /**
     * 注册参数
     */
    static public UserAccountInfoDO forSignup(String nickname, Sex sex, int age, String industry) {
        return new UserAccountInfoDO(0, nickname, sex, age, industry);
    }
}
