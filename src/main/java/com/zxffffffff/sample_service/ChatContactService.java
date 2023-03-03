/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_service;

import com.zxffffffff.sample_cache.ChatContactCacheDAO;
import com.zxffffffff.sample_db.ChatContactDAO;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatContactService {
    ChatContactDAO db = new ChatContactDAO("127.0.0.1", "root", "123456");
    ChatContactCacheDAO cache = new ChatContactCacheDAO("127.0.0.1");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    public ChatContactService() {

    }
}
