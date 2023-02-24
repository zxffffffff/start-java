package com.zxffffffff.sample_db;

import org.junit.jupiter.api.Test;

public class SampleMySQLTests {
    SampleMySQL sample = new SampleMySQL("127.0.0.1", "root", "123456", "test_db");

    @Test
    void contextLoads() {
        assert(this.sample != null);
    }

    @Test
    void testRunSql() {
        var res = this.sample.runSql("select * from user_base_info;");
    }
}
