package com.zxffffffff.sample_tools;

import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;


public class SnowFlakeTests {
    public static void main(String[] args) throws InterruptedException {
        SnowFlake idWorker = new SnowFlake(0, 0);
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            long id = idWorker.nextId();
            Thread.sleep(1);
            ids.add(id);
        }
        Assertions.assertEquals(ids.size(), 10);
    }
}
