package com.zxffffffff.sample_db;

import com.zxffffffff.sample_tools.SampleTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class SampleAccountDBTask implements Callable<Integer> {
    SampleAccountDB sample;
    int i;

    public SampleAccountDBTask(SampleAccountDB sample, int i) {
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
        String pwd_md5 = SampleTools.Hash(s, "MD5");

        // signup
        var id = sample.signup(user_name, user_email, user_phone, pwd_md5);
        Assertions.assertNotEquals(id, 0);
        try {
            sample.signup(user_name, user_email, user_phone, pwd_md5);
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
        return 0;
    }
}

public class SampleAccountDBTests {
    SampleAccountDB sample = new SampleAccountDB("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Test
    void testSignupLogin() {
        sample.truncateTable("user_account_pwd");

        /*
        user_account_pwd
            # id, create_time, update_time, user_id, user_password, user_name, user_phone, user_email
            1, 2023-02-28 19:20:05, 2023-02-28 19:20:05, 1080207782436663297, 69...c7, zxffff0001, 13000000001,
            2, 2023-02-28 19:20:05, 2023-02-28 19:20:05, 1080207782436663296, 76...1d, zxffff0060, 13000000060, zxffff0060@qq.com
            3, 2023-02-28 19:20:05, 2023-02-28 19:20:05, 1080207782507966464, 0a...8f, zxffff0007, 13000000007,
            4, 2023-02-28 19:20:05, 2023-02-28 19:20:05, 1080207782507966465, cb...bc, zxffff0017, 13000000017,
            5, 2023-02-28 19:20:05, 2023-02-28 19:20:05, 1080207782545715200, b5...66, zxffff0012, 13000000012, zxffff0012@qq.com
        */

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            var future = threadPool.submit(new SampleAccountDBTask(sample, i));
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
        }
    }
}
