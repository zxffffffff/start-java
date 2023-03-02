package com.zxffffffff.sample_db.DO;

/**
 * 用户账号密码（user_account_pwd）
 *
 * @param user_id      用户唯一id
 * @param user_name    用户名
 * @param user_email   邮箱，可以为""表示未填写
 * @param user_phone   手机号
 * @param user_pwd_md5 密码-MD5
 */
public record UserAccountPwdDO(long user_id, String user_name, String user_email, String user_phone,
                               String user_pwd_md5) {
    public UserAccountPwdDO {
        assert (user_name != null);
        assert (user_email != null);
        assert (user_phone != null);
        assert (user_pwd_md5 != null);
    }

    public UserAccountPwdDO() {
        this(0, "", "", "", "");
    }

    /**
     * 注册参数
     */
    static public UserAccountPwdDO forSignup(String user_name, String user_email, String user_phone, String user_pwd_md5) {
        return new UserAccountPwdDO(0, user_name, user_email, user_phone, user_pwd_md5);
    }
}
