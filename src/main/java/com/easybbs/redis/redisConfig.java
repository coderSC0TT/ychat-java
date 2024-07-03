package com.easybbs.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @ClassName redisConfig
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/3 下午11:14
 */
@Configuration
public class redisConfig<V> {
    @Bean("redisTemplate")
    public RedisTemplate<String,V> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String,V> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //key序列化
        template.setKeySerializer(RedisSerializer.string());
        //value序列化
        template.setValueSerializer(RedisSerializer.json());
        //hash KEY序列化
        template.setHashKeySerializer(RedisSerializer.string());
        //hash VALUE序列化
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}

