package com.zxffffffff.sample_db;

import com.zxffffffff.sample_tools.SampleTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleMySQLTests {
    SampleMySQL sample = new SampleMySQL("127.0.0.1", "root", "123456", "test_db");

    @Test
    void testAccountPwd() {
        sample.truncateTable("user_account_pwd");

        /* # id, create_time, update_time, user_id, user_password, user_name, user_phone, user_email
        '1', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076870352891904', '70...6', 'zxffff0000', '13000000000', 'zxffff0000@qq.com'
        '2', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076870529052672', '69...7', 'zxffff0001', '13000000001', NULL
        '3', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076870642298880', 'ff...0', 'zxffff0002', '13000000002', 'zxffff0002@qq.com'
        '4', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076870780710912', '5d...f', 'zxffff0003', '13000000003', NULL
        '5', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076870998814720', 'f9...c', 'zxffff0004', '13000000004', 'zxffff0004@qq.com'
        '6', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076871137226752', 'e3...4', 'zxffff0005', '13000000005', NULL
        '7', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076871221112832', '2e...6', 'zxffff0006', '13000000006', 'zxffff0006@qq.com'
        '8', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076871304998912', '0a...f', 'zxffff0007', '13000000007', NULL
        '9', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076871351136256', '8a...8', 'zxffff0008', '13000000008', 'zxffff0008@qq.com'
        '10', '2023-02-28 10:39:53', '2023-02-28 10:39:53', '1080076871418245120', 'e...9e', 'zxffff0009', '13000000009', NULL
        */

        for (int i = 0; i < 10; ++i) {
            String s = String.format("%04d", i);
            String user_name = "zxffff" + s;
            String user_email = "";
            if (i % 2 == 0)
                user_email  = "zxffff" + s + "@qq.com";
            String user_phone = "1300000" + s;
            String pwd_md5 = SampleTools.Hash(s, "MD5");

            var id = sample.signup(user_name, user_email, user_phone, pwd_md5);
            Assertions.assertNotEquals(id, 0);

            var id2 = sample.login(user_name, pwd_md5);
            Assertions.assertNotEquals(id2, 0);
            var id4 = sample.login(user_phone, pwd_md5);
            Assertions.assertNotEquals(id4, 0);
            if (i % 2 == 0) {
                var id3 = sample.login(user_email, pwd_md5);
                Assertions.assertNotEquals(id3, 0);
            }

            try {
                sample.login(user_name.replace('0', '1'), pwd_md5);
                Assertions.fail();
            } catch (RuntimeException e) {
                Assertions.assertTrue(true);
            }
            try {
                sample.login(user_phone.replace('0', '1'), pwd_md5);
                Assertions.fail();
            } catch (RuntimeException e) {
                Assertions.assertTrue(true);
            }
            try {
                sample.login(user_email.replace('0', '1'), pwd_md5);
                Assertions.fail();
            } catch (RuntimeException e) {
                Assertions.assertTrue(true);
            }
        }
    }
}
