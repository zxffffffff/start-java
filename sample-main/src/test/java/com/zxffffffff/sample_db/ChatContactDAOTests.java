/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_db;

import com.zxffffffff.DO.ChatContactsAddDO;
import com.zxffffffff.DO.ChatContactsMessageDO;
import com.zxffffffff.sample_tools.BaseTools;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

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

class AddContactTask implements Callable<Integer> {
    ChatContactDAO sample;

    public AddContactTask(ChatContactDAO sample) {
        this.sample = sample;
    }

    @Override
    public Integer call() {
        long user_id = BaseTools.generateSnowFlakeID();
        long contact_id = BaseTools.generateSnowFlakeID();
        long contact_id2 = BaseTools.generateSnowFlakeID();

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
            List<ChatContactsAddDO> list = sample.getContactAddList(user_id);
            Assertions.assertEquals(list.size(), 2);
            ChatContactsAddDO p2 = list.get(0);
            ChatContactsAddDO p = list.get(1);
            Assertions.assertEquals(p.contact_user_id(), contact_id);
            Assertions.assertEquals(p2.contact_user_id(), contact_id2);
            Assertions.assertEquals(p.add_status(), ChatContactsAddDO.AddStatus.Waiting);
            Assertions.assertEquals(p2.add_status(), ChatContactsAddDO.AddStatus.Waiting);
        }

        // handleAddContact
        sample.setContactAddStatus(user_id, contact_id, ChatContactsAddDO.AddStatus.Accepted);
        sample.setContactAddStatus(user_id, contact_id2, ChatContactsAddDO.AddStatus.Refused);
        try {
            sample.setContactAddStatus(user_id, contact_id, ChatContactsAddDO.AddStatus.Accepted);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }
        try {
            sample.setContactAddStatus(user_id, contact_id2, ChatContactsAddDO.AddStatus.Refused);
            Assertions.fail();
        } catch (RuntimeException e) {
            Assertions.assertTrue(true);
        }

        // getAddContacts
        {
            List<ChatContactsAddDO> list = sample.getContactAddList(user_id);
            Assertions.assertEquals(list.size(), 2);
            ChatContactsAddDO p2 = list.get(0);
            ChatContactsAddDO p = list.get(1);
            Assertions.assertEquals(p.contact_user_id(), contact_id);
            Assertions.assertEquals(p2.contact_user_id(), contact_id2);
            Assertions.assertEquals(p.add_status(), ChatContactsAddDO.AddStatus.Accepted);
            Assertions.assertEquals(p2.add_status(), ChatContactsAddDO.AddStatus.Refused);
        }

        // getContactList
        {
            List<Long> list = sample.getContactList(user_id);
            Assertions.assertEquals(list.size(), 1);
            Assertions.assertEquals(list.get(0), contact_id);
        }

        // setContactMessage
        List<ChatContactsMessageDO> listMessage = new ArrayList<>();
        List<ChatContactsMessageDO> listMessage2 = new ArrayList<>();
        int msgSize = 10;
        for (int im = 0; im < msgSize; ++im) {
            String message = "message" + im;

            ChatContactsMessageDO messageDO = ChatContactsMessageDO.forSetText(user_id, contact_id, message);
            sample.setContactMessage(messageDO);
            listMessage.add(messageDO);

            ChatContactsMessageDO messageDO2 = ChatContactsMessageDO.forSetText(contact_id, user_id, message);
            sample.setContactMessage(messageDO2);
            listMessage2.add(messageDO);
            listMessage2.add(messageDO2);
        }
        Collections.reverse(listMessage);
        Collections.reverse(listMessage2);

        // getContactMessage
        List<ChatContactsMessageDO> contactMessage = sample.getContactMessage(user_id, contact_id, 0, 100);
        Assertions.assertEquals(contactMessage.size(), msgSize);
        for (int im = 0; im < msgSize; ++im) {
            Assertions.assertEquals(contactMessage.get(im).msg_text(), listMessage.get(im).msg_text());
        }

        // getContactMessage2
        List<ChatContactsMessageDO> contactMessage2 = sample.getContactMessage2(user_id, contact_id, 0, 100);
        Assertions.assertEquals(contactMessage2.size(), msgSize * 2);
        for (int im = 0; im < msgSize; ++im) {
            Assertions.assertEquals(contactMessage2.get(im).msg_text(), listMessage2.get(im).msg_text());
        }

        return 123;
    }
}

public class ChatContactDAOTests {
    ChatContactDAO sample = new ChatContactDAO("127.0.0.1", "root", "123456");
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Test
    void addContactTest() {
        sample.truncateTable("chat_contacts_add");
        sample.truncateTable("chat_contacts");
        sample.truncateTable("chat_contacts_message");

        var task = new AddContactTask(sample);
        task.call();
    }

    @Test
    void addContactTestThread() {
        sample.truncateTable("chat_contacts_add");
        sample.truncateTable("chat_contacts");
        sample.truncateTable("chat_contacts_message");

        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            var future = threadPool.submit(new AddContactTask(sample));
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
                Assertions.assertEquals(future.get(), 123);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
