package com.zxffffffff.sample_db;

import com.zxffffffff.sample_tools.SampleTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleMySQLTests {
    SampleMySQL sample = new SampleMySQL("127.0.0.1", "root", "123456", "test_db");

    @Test
    void testRunSql() {
        sample.forceDeleteUserDB();

        String pwd_md5 = SampleTools.Hash("123", "MD5");
        var id = sample.signup("zxffffffff", "1337328542@qq.com", "13000000000", pwd_md5);
        Assertions.assertNotEquals(id, 0);

        var id2 = sample.login("zxffffffff", pwd_md5);
        Assertions.assertNotEquals(id2, 0);
        var id3 = sample.login("1337328542@qq.com", pwd_md5);
        Assertions.assertNotEquals(id3, 0);
        var id4 =sample.login("13000000000", pwd_md5);
        Assertions.assertNotEquals(id4, 0);

        try {
            sample.login("13000000001", pwd_md5);
            Assertions.fail();
        }
        catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }
    }
}
