/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.sample_db.DO.UserAccountInfoDO;
import com.zxffffffff.sample_db.DO.UserAccountPwdDO;
import com.zxffffffff.sample_tools.BaseTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/*

user_account_pwd
# id, create_time, update_time, user_id, user_password, user_name, user_phone, user_email
1, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466373136384, 1...1, zxffff0059, 13000000059,
2, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466373136385, 2...f, zxffff0039, 13000000039,
3, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466486382593, 7...d, zxffff0060, 13000000060, zxffff0060@qq.com
4, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466486382592, 1...3, zxffff0052, 13000000052, zxffff0052@qq.com
5, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466524131328, 6...b, zxffff0058, 13000000058, zxffff0058@qq.com

user_account_info
# id, create_time, update_time, user_id, nickname, sex, age, industry, id_card, others
1, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466373136385, nick0039, , 59, , ,
2, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466373136384, nick0059, 2, , 土木, ,
3, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466486382592, nick0052, 1, , IT/互联网, ,
4, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466486382593, nick0060, , 80, , ,
5, 2023-03-01 16:10:31, 2023-03-01 16:10:31, 1080522466524131328, nick0058, 1, , IT/互联网, ,

*/

class SignupLoginTask implements Callable<Long> {
    UserAccountService sample;
    int i;

    public SignupLoginTask(UserAccountService sample, int i) {
        this.sample = sample;
        this.i = i;
    }

    @Override
    public Long call() {
        assert (i < 1000);
        String s = String.format("%04d", i);
        String user_name = "zxffff" + s;
        String user_email = "";
        if (i % 2 == 0)
            user_email = "zxffff" + s + "@qq.com";
        String user_phone = "1300000" + s;
        String pwd_md5 = BaseTools.Hash(s, "MD5");
        String user_nickname = "nick" + s;
        UserAccountInfoDO.Sex sex = UserAccountInfoDO.Sex.Undefined;
        if (i % 3 == 1)
            sex = UserAccountInfoDO.Sex.Male;
        else if (i % 3 == 2)
            sex = UserAccountInfoDO.Sex.Female;
        int age = -1;
        if (i % 3 == 0)
            age = 20 + i;
        String industry = "";
        if (i % 3 == 1)
            industry = "IT/互联网";
        else if (i % 3 == 2)
            industry = "土木";

        UserAccountPwdDO pwdDO = UserAccountPwdDO.forSignup(user_name, user_email, user_phone, pwd_md5);
        UserAccountInfoDO infoDO = UserAccountInfoDO.forSignup(user_nickname, sex, age, industry);

        // signup
        var id = sample.signup(pwdDO, infoDO);
        Assertions.assertNotEquals(id, 0);
        try {
            sample.signup(pwdDO, infoDO);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }

        // login
        var id2 = sample.login(user_name, pwd_md5);
        Assertions.assertEquals(id2, id);
        var id4 = sample.login(user_phone, pwd_md5);
        Assertions.assertEquals(id4, id);
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

        // getInfo
        UserAccountInfoDO infoDO2 = sample.getInfo(id);
        Assertions.assertEquals(infoDO.nickname(), infoDO2.nickname());
        Assertions.assertEquals(infoDO.sex(), infoDO2.sex());
        Assertions.assertEquals(infoDO.age(), infoDO2.age());
        Assertions.assertEquals(infoDO.industry(), infoDO2.industry());

        return id;
    }
}

public class UserAccountServiceTests {
    UserAccountService sample = new UserAccountService("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Test
    void signupLoginTest() {
        sample.truncateTable("user_account_pwd");
        sample.truncateTable("user_account_info");

        var task = new SignupLoginTask(sample, 888);
        task.call();
    }

    @Test
    void signupLoginTestThread() {
        sample.truncateTable("user_account_pwd");
        sample.truncateTable("user_account_info");

        List<Future<Long>> futures = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            var future = threadPool.submit(new SignupLoginTask(sample, i));
            futures.add(future);
        }
        for (var future : futures) {
            while (!future.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Assertions.assertNotEquals(future.get(), 0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    void getInfoListTest() {
        sample.truncateTable("user_account_pwd");
        sample.truncateTable("user_account_info");

        var task1 = new SignupLoginTask(sample, 888);
        var task2 = new SignupLoginTask(sample, 999);
        long id1 = task1.call();
        long id2 = task2.call();

        List<Long> list = new ArrayList<>(Arrays.asList(id1, id2));

        List<UserAccountInfoDO> infoList = sample.getInfoList(list);
        Assertions.assertEquals(infoList.size(), 2);
        Assertions.assertEquals(infoList.get(0).user_id(), id2);
        Assertions.assertEquals(infoList.get(1).user_id(), id1);
    }
}
