/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleToolsTests {
    @Test
    void testCheckIsValidName() {
        Assertions.assertTrue(SampleTools.checkIsValidName("a2345678"));
        Assertions.assertTrue(SampleTools.checkIsValidName("a234_678"));
        Assertions.assertTrue(SampleTools.checkIsValidName("aaaaaaaa"));
        Assertions.assertTrue(SampleTools.checkIsValidName("aaaa_aaa"));

        Assertions.assertFalse(SampleTools.checkIsValidName("a234567"));
        Assertions.assertFalse(SampleTools.checkIsValidName("12345678"));
        Assertions.assertFalse(SampleTools.checkIsValidName("aaaaaaa"));
        Assertions.assertFalse(SampleTools.checkIsValidName("啊aaaaaaa"));
        Assertions.assertFalse(SampleTools.checkIsValidName("aaaaaaa啊"));
        Assertions.assertFalse(SampleTools.checkIsValidName("aaaa$aaa"));
    }

    @Test
    void testCheckIsValidMail() {
        Assertions.assertTrue(SampleTools.checkIsValidMail("12345678@qq.com"));
        Assertions.assertTrue(SampleTools.checkIsValidMail("a2345678_xx@123.com"));

        Assertions.assertFalse(SampleTools.checkIsValidMail("a2345678_xx123@.com"));
        Assertions.assertFalse(SampleTools.checkIsValidMail("a2345678_xx#123.com"));
    }

    @Test
    void testCheckIsValidPhone() {
        Assertions.assertTrue(SampleTools.checkIsValidPhone("13000000000"));
        Assertions.assertTrue(SampleTools.checkIsValidPhone("13500000000"));

        Assertions.assertFalse(SampleTools.checkIsValidPhone("23000000000"));
        Assertions.assertFalse(SampleTools.checkIsValidPhone("1300000x000"));
    }

}
