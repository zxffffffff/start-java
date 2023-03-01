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
import java.util.List;
import java.util.concurrent.*;

class SignupLoginTask implements Callable<Integer> {
    UserAccountService sample;
    int i;

    public SignupLoginTask(UserAccountService sample, int i) {
        this.sample = sample;
        this.i = i;
    }

    @Override
    public Integer call() {
        String s = String.format("%04d", i);
        String user_name = "zxffff" + s;
        String user_email = "";
        if (i % 2 == 0)
            user_email = "zxffff" + s + "@qq.com";
        String user_phone = "1300000" + s;
        String pwd_md5 = BaseTools.Hash(s, "MD5");
        String user_nickname = "nick" + s;

        UserAccountPwdDO pwdDO = UserAccountPwdDO.forSignup(user_name, user_email, user_phone, pwd_md5);
        UserAccountInfoDO infoDO = UserAccountInfoDO.forSignup(user_nickname);

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

        return 123;
    }
}

public class UserAccountServiceTests {
    UserAccountService sample = new UserAccountService("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    /*
    user_account_pwd
        # id, create_time, update_time, user_id, user_password, user_name, user_phone, user_email
        1, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651194277889, e...4, zxffff0005, 13000000005,
        2, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651215249409, 2...e, zxffff0038, 13000000038, zxffff0038@qq.com
        3, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651215249408, 5...2, zxffff0014, 13000000014, zxffff0014@qq.com
        4, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651194277888, 9...6, zxffff0035, 13000000035,
        5, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651236220928, e...9, zxffff0026, 13000000026, zxffff0026@qq.com

    user_account_info
        # id, create_time, update_time, user_id, nickname, sex, age, industry, id_card, others
        1, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651194277889, nick0005, , , , ,
        2, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651215249408, nick0014, , , , ,
        3, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651236220928, nick0026, , , , ,
        4, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651194277888, nick0035, , , , ,
        5, 2023-03-01 14:16:01, 2023-03-01 14:16:01, 1080493651215249409, nick0038, , , , ,
    */

    //@Test
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

        List<Future<Integer>> futures = new ArrayList<>();
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
}
