package com.example.sharding.msterslave.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Method
 * @Author yakun.shi
 * @Description
 * @Return
 * @Date 2019/9/27 11:48
 */
@Data
public class LogTest {
    private Integer id;
    private String message;
    private Date createtime;
}
