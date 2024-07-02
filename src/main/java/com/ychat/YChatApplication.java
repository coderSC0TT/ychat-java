package com.ychat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @ClassName YChatApplication
 * @Description 启动类
 * @Author Suguo
 * @LastChangeDate 2024/7/1 下午8:46
 */
@SpringBootApplication(scanBasePackages = {"com.ychat"},exclude = DataSourceAutoConfiguration.class)
public class YChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(YChatApplication.class, args);
    }
}

