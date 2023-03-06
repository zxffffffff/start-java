/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_service;

import com.zxffffffff.sample_db.UserAccountDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserAccountService {
    UserAccountDAO db = new UserAccountDAO("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public UserAccountService() {

    }
}
