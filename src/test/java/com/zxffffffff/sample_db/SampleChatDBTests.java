/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.sample_tools.SampleTools;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class SampleChatDBTask implements Callable<Integer> {
    SampleChatDB sample;

    public SampleChatDBTask(SampleChatDB sample) {
        this.sample = sample;
    }

    @Override
    public Integer call() {
        long user_id = SampleTools.generateSnowFlakeID();
        long contact_id = SampleTools.generateSnowFlakeID();
        long contact_id2 = SampleTools.generateSnowFlakeID();

        // addContact
        sample.addContactAdd(user_id, contact_id, "test message!");
        sample.addContactAdd(user_id, contact_id2, "test message!");
        try {
            sample.addContactAdd(user_id, contact_id, "test message!");
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }
        try {
            sample.addContactAdd(user_id, contact_id2, "test message!");
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }

        // getAddContacts
        {
            List<Pair<Long, SampleChatDB.EnContactAddStatus>> list = sample.getContactAddList(user_id);
            Assertions.assertEquals(list.size(), 2);
            Pair<Long, SampleChatDB.EnContactAddStatus> p2 = list.get(0);
            Pair<Long, SampleChatDB.EnContactAddStatus> p = list.get(1);
            Assertions.assertEquals(p.getLeft(), contact_id);
            Assertions.assertEquals(p2.getLeft(), contact_id2);
            Assertions.assertEquals(p.getRight(), SampleChatDB.EnContactAddStatus.Waiting);
            Assertions.assertEquals(p2.getRight(), SampleChatDB.EnContactAddStatus.Waiting);
        }

        // handleAddContact
        sample.setContactAddStatus(user_id, contact_id, SampleChatDB.EnContactAddStatus.Accepted);
        sample.setContactAddStatus(user_id, contact_id2, SampleChatDB.EnContactAddStatus.Refused);
        try {
            sample.setContactAddStatus(user_id, contact_id, SampleChatDB.EnContactAddStatus.Accepted);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }
        try {
            sample.setContactAddStatus(user_id, contact_id2, SampleChatDB.EnContactAddStatus.Refused);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }

        // getAddContacts
        {
            List<Pair<Long, SampleChatDB.EnContactAddStatus>> list = sample.getContactAddList(user_id);
            Assertions.assertEquals(list.size(), 2);
            Pair<Long, SampleChatDB.EnContactAddStatus> p2 = list.get(0);
            Pair<Long, SampleChatDB.EnContactAddStatus> p = list.get(1);
            Assertions.assertEquals(p.getLeft(), contact_id);
            Assertions.assertEquals(p2.getLeft(), contact_id2);
            Assertions.assertEquals(p.getRight(), SampleChatDB.EnContactAddStatus.Accepted);
            Assertions.assertEquals(p2.getRight(), SampleChatDB.EnContactAddStatus.Refused);
        }
        return 0;
    }
}

public class SampleChatDBTests {
    SampleChatDB sample = new SampleChatDB("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Test
    void testAddContact() {
        sample.truncateTable("chat_contacts_add");
        sample.truncateTable("chat_contacts");

        /*
        chat_contacts_add
            # id, create_time, update_time, user_id, contact_user_id, add_msg, add_status
            1, 2023-02-28 19:35:32, 2023-02-28 19:35:32, 1080211672095780864, 1080211672095780865, test message!, 2
            2, 2023-02-28 19:35:32, 2023-02-28 19:35:33, 1080211672049643529, 1080211672049643530, test message!, 2
            3, 2023-02-28 19:35:32, 2023-02-28 19:35:32, 1080211672049643532, 1080211672049643533, test message!, 2
            4, 2023-02-28 19:35:32, 2023-02-28 19:35:33, 1080211672095780864, 1080211672095780866, test message!, 3
            5, 2023-02-28 19:35:32, 2023-02-28 19:35:33, 1080211672049643529, 1080211672049643531, test message!, 3

        chat_contacts
            # id, create_time, update_time, user_id, contact_user_id, contact_user_privacy, contact_user_nickname
            1, 2023-02-28 19:19:03, 2023-02-28 19:19:03, 1080207519776763925, 1080207519776763926, 0,
            2, 2023-02-28 19:19:03, 2023-02-28 19:19:03, 1080207519776763934, 1080207519776763935, 0,
            3, 2023-02-28 19:19:03, 2023-02-28 19:19:03, 1080207519776763931, 1080207519776763932, 0,
            4, 2023-02-28 19:19:03, 2023-02-28 19:19:03, 1080207519776763910, 1080207519776763911, 0,
            5, 2023-02-28 19:19:03, 2023-02-28 19:19:03, 1080207519780958211, 1080207519780958212, 0,
        */

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            var future = threadPool.submit(new SampleChatDBTask(sample));
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
