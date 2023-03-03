/****************************************************************************
 ** MIT License
 **
 ** Author	: xiaofeng.zhu
 ** Support	: zxffffffff@outlook.com, 1337328542@qq.com
 **
 ****************************************************************************/

package com.zxffffffff.sample_tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

class SnowFlakeTask implements Callable<Set<Long>> {
    SnowFlake idWorker;
    int size;

    public SnowFlakeTask(SnowFlake idWorker, int size) {
        this.idWorker = idWorker;
        this.size = size;
    }

    @Override
    public Set<Long> call() {
        Set<Long> set = new HashSet<>();
        for (int i = 0; i < size; i++) {
            long id = idWorker.nextId();
            set.add(id);
        }
        return set;
    }
}

public class SnowFlakeTests {
    SnowFlake idWorker = new SnowFlake(0, 0);
    ExecutorService threadPool = Executors.newFixedThreadPool(64);

    @Test
    void testSnowFlake() {
        int size = 999999;
        var task = new SnowFlakeTask(idWorker, size);
        Set<Long> set = task.call();
        Assertions.assertEquals(size, set.size());
    }

    @Test
    void testSnowFlakeThread() {
        int size = 99999;
        List<Future<Set<Long>>> futures = new ArrayList<>();
        for (int i = 0; i < 64; ++i) {
            var future = threadPool.submit(new SnowFlakeTask(idWorker, size));
            futures.add(future);
        }
        Set<Long> set = new HashSet<>();
        for (var future : futures) {
            while (!future.isDone()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                set.addAll(future.get());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        Assertions.assertEquals(set.size(), size * 64);
    }
}
