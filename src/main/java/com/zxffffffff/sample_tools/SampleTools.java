package com.zxffffffff.sample_tools;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SampleTools {
    // 单例
    private static final SampleTools instance = new SampleTools();

    // 单例
    private SampleTools() {
    }

    // 单例
    public static SampleTools singleton() {
        return instance;
    }

    // 雪花算法
    SnowFlake idWorker = new SnowFlake(31, 31);

    /**
     * 帐号格式是否合法
     *
     * @param name 字母开头，字母数字下划线，长度[8,256]
     * @return true:合法
     */
    public static boolean checkIsValidName(String name) {
        return name.matches("^[a-zA-Z][a-zA-Z0-9_]{7,255}$");
    }

    /**
     * Email格式是否合法
     *
     * @param mail 邮箱
     * @return true:合法
     */
    public static boolean checkIsValidMail(String mail) {
        return mail.matches("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$");
    }

    /**
     * 电话（国内）格式是否合法
     *
     * @param phone 手机号
     * @return true:合法
     */
    public static boolean checkIsValidPhone(String phone) {
        return phone.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$");
    }

    /**
     * 雪花算法生成唯一id
     *
     * @return 64位id
     */
    public static long createSnowFlakeID() {
        return singleton().idWorker.nextId();
    }

    /** 各种Hash算法
     * @param strText 任意字符
     * @param strType "SHA-256" "SHA-512" "MD5" 等
     * @return byte[]转String
     */
    public static String Hash(final String strText, final String strType) {
        String strResult = null;
        if (strText != null && strText.length() > 0) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(strType);
                messageDigest.update(strText.getBytes());
                byte[] byteBuffer = messageDigest.digest();

                StringBuilder strHexString = new StringBuilder();
                for (byte b : byteBuffer) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        strHexString.append('0');
                    }
                    strHexString.append(hex);
                }
                strResult = strHexString.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return strResult;
    }

    public static String getSaltPassword(String pwd_md5) {
        String salt_password = Hash(pwd_md5 + Hash(pwd_md5, "SHA-512"), "SHA-256");
        assert (salt_password.length() == 64);
        return salt_password;
    }
}
