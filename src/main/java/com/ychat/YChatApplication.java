package com.ychat;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @ClassName YChatApplication
 * @Description 启动类
 * @Author Suguo
 * @LastChangeDate 2024/7/1 下午8:46
 */
@SpringBootApplication(scanBasePackages = {"com.ychat"},exclude = DataSourceAutoConfiguration.class)
@MapperScan(basePackages  ={"com.easybbs.mappers"})
@EnableTransactionManagement
@EnableScheduling
@EnableAsync
public class YChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(YChatApplication.class, args);
    }
}

