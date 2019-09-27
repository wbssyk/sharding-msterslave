package com.example.sharding.msterslave.dao.write;

import com.example.sharding.msterslave.entity.LogTest;

import java.util.List;
import java.util.Map;

/**
 * @Method
 * @Author yakun.shi
 * @Description
 * @Return
 * @Date 2019/9/27 11:51
 */
public interface LogTestWriteMapper {
    void insert(LogTest testDb);
}
