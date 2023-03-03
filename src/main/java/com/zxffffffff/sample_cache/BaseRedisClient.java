/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class BaseRedisClient {
    protected final JedisPool pool;

    public BaseRedisClient(String host) {
        pool = new JedisPool(host, 6379);
    }
}
