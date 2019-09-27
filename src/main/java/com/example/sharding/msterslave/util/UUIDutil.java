package com.example.sharding.msterslave.util;

import java.util.UUID;

/**
 * @author yakun.shi
 * @create 2019/9/20 10:02
 */
public class UUIDutil {
    /**
     * 生成id
     * @return
     */
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
