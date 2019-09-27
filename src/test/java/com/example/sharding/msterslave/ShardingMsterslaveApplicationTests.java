package com.example.sharding.msterslave;

import com.example.sharding.msterslave.dao.read.LogTestReadMapper;
import com.example.sharding.msterslave.dao.write.LogTestWriteMapper;
import com.example.sharding.msterslave.entity.LogTest;
import com.example.sharding.msterslave.util.DatetimeUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ShardingMsterslaveApplicationTests {


    @Autowired
    private LogTestReadMapper logTestReadMapper;

    @Autowired
    private LogTestWriteMapper logTestWriteMapper;

    @Test
    public void getLogs() {
        List<Map<String, Object>> db = logTestReadMapper.getDb();
        System.out.println(db);
    }


    @Test
    public void insertLogs() {

        LogTest logTest1 = new LogTest();
        logTest1.setMessage("8月份日志");
        logTest1.setCreatetime(DatetimeUtil.getDate("2019-08-10","yyyy-MM-dd"));
        logTestWriteMapper.insert(logTest1);


        LogTest logTest2 = new LogTest();
        logTest2.setMessage("11月份日志");
        logTest2.setCreatetime(DatetimeUtil.getDate("2019-11-10","yyyy-MM-dd"));
        logTestWriteMapper.insert(logTest2);

    }


}
