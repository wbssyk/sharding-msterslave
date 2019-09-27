package com.example.sharding.msterslave.config;

import com.example.sharding.msterslave.util.DatetimeUtil;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * @program: sharding-jdbc
 * @description: 分表算法
 * @author: yaKun.shi
 * @create: 2019-09-20 09:38
 **/
@Component
@Log4j2
public class TableShardingAlgorithm implements PreciseShardingAlgorithm<Date> {

    @Autowired
    @Qualifier("master")
    private DataSource masterDataSource;

    @Override
    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Date> shardingValue) {
        Date value = shardingValue.getValue();
        String suffixName = DatetimeUtil.formatDate(value, "YYYYMM");
        String tableName = getRoutTable("log", suffixName);
        ;
        for (String s : availableTargetNames) {
            if (s.equals("log")) {
                tableName = createTable("log", suffixName);
            }
        }
        return tableName;
    }


    private String createTable(String logicTable, String suffixName) {

        // 需要生成的表名  log_201909
        String tableName = getRoutTable(logicTable, suffixName);
        Connection connection = null;
        Statement stmt = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = masterDataSource.getConnection();
            String aa = "'" + tableName + "'";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("show tables like" + aa);
            if (!resultSet.next()) {
                // 动态创建日志表(根据月份来分)
                String sql = "CREATE TABLE IF NOT EXISTS " + tableName +
                        "(id VARCHAR(64) not null," +
                        "message VARCHAR(64)," +
                        "createtime datetime," +
                        "PRIMARY KEY (id))" +
                        "ENGINE=InnoDB DEFAULT CHARSET=utf8;";
                stmt = connection.createStatement();
                if (0 == stmt.executeUpdate(sql)) {
                    log.info("成功创建表！");
                    String insertSql = "INSERT INTO table_name (tablename) VALUES (?)";
                    preparedStatement = connection.prepareStatement(insertSql);
                    preparedStatement.setString(1, tableName);
                    preparedStatement.execute();
                    log.info("记录分库表名成功");
                } else {
                    log.info("创建表失败！");
                }
            }else {
               log.info("表已经存在！");
            }
        } catch (SQLException e) {
            log.error("错误---->{}",e);
        } finally {
            try {
                if(stmt!=null){
                    stmt.close();
                }if(preparedStatement!=null){
                    preparedStatement.close();
                }if(connection!=null){
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            log.info("关闭资源");
        }
        return tableName;
    }

    private String getRoutTable(String logicTable, Date keyValue) {
        if (keyValue != null) {
            String formatDate = DatetimeUtil.formatDate(keyValue, "YYYYMM");
            return logicTable + formatDate;
        }
        return null;

    }

    private String getRoutTable(String logicTable, String suffix) {
        return logicTable + "_" + suffix;
    }

    private Collection<String> getRoutTable(String logicTable, Date lowerEnd, Date upperEnd) {
        Set<String> routTables = new HashSet<String>();
        if (lowerEnd != null && upperEnd != null) {
            List<String> rangeNameList = getRangeNameList(lowerEnd, upperEnd);
            for (String string : rangeNameList) {
                routTables.add(logicTable + string);
            }
        }
        return routTables;
    }

    private static List<String> getRangeNameList(Date start, Date end) {
        List<String> result = Lists.newArrayList();
        // 定义日期实例
        Calendar dd = Calendar.getInstance();
        // 设置日期起始时间
        dd.setTime(start);
        // 判断是否到结束日期
        while (dd.getTime().before(end)) {
            SimpleDateFormat sdf = new SimpleDateFormat("YYYYMM");
            String str = sdf.format(dd.getTime());
            result.add(str);
            // 进行当前日期月份加1
            dd.add(Calendar.MONTH, 1);
        }
        return result;
    }
}
