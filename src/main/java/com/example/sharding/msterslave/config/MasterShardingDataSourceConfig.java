package com.example.sharding.msterslave.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.MasterSlaveDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: sharding-jdbc
 * @description: 分库分表配置文件
 * @author: yaKun.shi
 * @create: 2019-09-19 09:40
 **/
@Configuration
@MapperScan(value = "com.example.sharding.msterslave.dao.write",
        sqlSessionFactoryRef = "masterSqlSessionFactory")
public class MasterShardingDataSourceConfig {

    private final static String testLogicTable = "log";

    @Autowired
    private TableShardingAlgorithm tableShardingAlgorithm;
    /**
     * 配置主库，数据源的名称最好要有一定的规则，方便配置分库的计算规则
     *
     * @return
     * @date 2019-09-19 09:40
     */
    @Bean(name = "master")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    /**
     * shardingjdbc数据源
     *
     * @return
     * @throws SQLException
     * @date 2019-09-19 09:40
     */
    @Bean("masterShardIngDataSource")
    public DataSource shardIngDataSource(@Qualifier("master") DataSource masterDataSource) throws SQLException {
        // 第一步: 获取众多数据源  配置真实数据源
        Map<String, DataSource> dataSourceMap = new HashMap<>(16);
        // key 值必须和数据库名一致，不然会出现问题
        dataSourceMap.put("browser_log", masterDataSource);
        // 第三步: 获取总配置类 切片规则
        ShardingRuleConfiguration shardIngRuleConfig = new ShardingRuleConfiguration();

        // 第四步: 获取其余配置信息(如果需要的话)
        Properties properties = getProperties();

        // 第五步: 定制指定逻辑表的切片(分库分表)策略
        List<TableRuleConfiguration> allTableRuleConfiguration = getAllTableRuleConfiguration();

        // 第六步: 将定制了自己的切片策略的表的配置规则，加入总配置中
        shardIngRuleConfig.setTableRuleConfigs(allTableRuleConfiguration);

        // 第吧步: 创建并返回shardIng总数据源,注入容器
        return ShardingDataSourceFactory.createDataSource(dataSourceMap, shardIngRuleConfig, properties);
    }


    /**
     * 对指定逻辑表进行切片(分库分表)个性化配置
     * 注:这里只配置了定制的
     *
     * @return 指定表的分库分表配置
     * @date 2019-09-19 09:40
     */
    private List<TableRuleConfiguration> getAllTableRuleConfiguration() {
        List<TableRuleConfiguration> list = new ArrayList<>(8);
        TableRuleConfiguration masterLogRuleConfig = getMasterLogRuleConfig();
        list.add(masterLogRuleConfig);
        return list;
    }

    /**
     * log表分片策略
     *
     * @return 指定表的 分片策略
     * @date 2019-09-19 09:40
     */
    private TableRuleConfiguration getMasterLogRuleConfig() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(testLogicTable);
        //分布式id配置使用  使用雪花算法
        KeyGeneratorConfiguration keyGeneratorConfiguration = new
                KeyGeneratorConfiguration("SNOWFLAKE", "id");
        tableRuleConfig.setKeyGeneratorConfig(keyGeneratorConfiguration);
        // 配置分表策略(只在主库中分表)
        // 使用StandardShardingStrategyConfiguration方法来分库
        tableRuleConfig.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("createtime", tableShardingAlgorithm));
        return tableRuleConfig;
    }


    /**
     * 获取其余配置信息
     *
     * @return 其余配置信息
     * @date 2019-09-19 09:40
     */
    private Properties getProperties() {
        Properties properties = new Properties();
        // 打印出分库路由后的sql
        properties.setProperty("sql.show", "true");
        return properties;
    }

    /**
     * 获取sqlSessionFactory实例
     *
     * @param shardIngDataSource
     * @return
     * @throws Exception
     * @date 2019-09-19 09:40
     */
    @Bean("masterSqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("masterShardIngDataSource") DataSource shardIngDataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(shardIngDataSource);
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().
                getResources("classpath:/mapper/write/*.xml"));
        return bean.getObject();
    }

    @Bean("masterSqlSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("masterSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 需要手动配置事务管理器
     *
     * @param shardIngDataSource
     * @return
     * @date 2019-09-19 09:40
     */
    @Bean("masterDataSourceTransactionManager")
    public DataSourceTransactionManager transactionalManager(@Qualifier("masterShardIngDataSource") DataSource shardIngDataSource) {
        return new DataSourceTransactionManager(shardIngDataSource);
    }

}
