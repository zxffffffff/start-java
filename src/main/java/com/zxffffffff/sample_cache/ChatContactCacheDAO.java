/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_cache;

/**
 * 聊天IM相关
 * 线程安全，可重入
 */
public class ChatContactCacheDAO extends BaseRedisClient {
    public ChatContactCacheDAO(String host) {
        super(host);
    }
}
