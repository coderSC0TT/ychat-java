package com.easybbs;

import com.easybbs.redis.RedisUtils;
import io.lettuce.core.RedisConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * @ClassName InitRun
 * @Description 启动描述
 * @Author Suguo
 * @LastChangeDate 2024/7/6 下午2:56
 */
@Component("initRun")
public class InitRun  implements ApplicationRunner {

    private  static  final Logger logger =  LoggerFactory.getLogger(InitRun.class);
    @Resource
    private  DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Override
    public void run(ApplicationArguments args)  {
        try{
            dataSource.getConnection();
            String test = "\"testjson\"";
            redisUtils.get(test);
            logger.info("服务启动成功");
        }catch (SQLException e){
            logger.error("数据库连接失败 请检查数据库配置");
        }catch (RedisConnectionFailureException e){
            logger.error("redis连接失败 请检查数据库配置");
        }catch (Exception e){
            logger.error("服务启动失败",e);
        }
    }
}

