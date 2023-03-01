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

public class BaseToolsTests {
    @Test
    void testCheckIsValidName() {
        Assertions.assertTrue(BaseTools.checkIsValidName("a2345678"));
        Assertions.assertTrue(BaseTools.checkIsValidName("a234_678"));
        Assertions.assertTrue(BaseTools.checkIsValidName("aaaaaaaa"));
        Assertions.assertTrue(BaseTools.checkIsValidName("aaaa_aaa"));

        Assertions.assertFalse(BaseTools.checkIsValidName("a234567"));
        Assertions.assertFalse(BaseTools.checkIsValidName("12345678"));
        Assertions.assertFalse(BaseTools.checkIsValidName("aaaaaaa"));
        Assertions.assertFalse(BaseTools.checkIsValidName("啊aaaaaaa"));
        Assertions.assertFalse(BaseTools.checkIsValidName("aaaaaaa啊"));
        Assertions.assertFalse(BaseTools.checkIsValidName("aaaa$aaa"));
    }

    @Test
    void testCheckIsValidMail() {
        Assertions.assertTrue(BaseTools.checkIsValidMail("12345678@qq.com"));
        Assertions.assertTrue(BaseTools.checkIsValidMail("a2345678_xx@123.com"));

        Assertions.assertFalse(BaseTools.checkIsValidMail("a2345678_xx123@.com"));
        Assertions.assertFalse(BaseTools.checkIsValidMail("a2345678_xx#123.com"));
    }

    @Test
    void testCheckIsValidPhone() {
        Assertions.assertTrue(BaseTools.checkIsValidPhone("13000000000"));
        Assertions.assertTrue(BaseTools.checkIsValidPhone("13500000000"));

        Assertions.assertFalse(BaseTools.checkIsValidPhone("23000000000"));
        Assertions.assertFalse(BaseTools.checkIsValidPhone("1300000x000"));
    }

}
