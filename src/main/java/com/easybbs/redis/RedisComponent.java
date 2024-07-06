package com.easybbs.redis;

import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SysSettingDto;
import com.easybbs.entity.dto.TokenUserInfoDto;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName redisComponent
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/5 下午9:20
 */
@Component("redisComponent")
public class RedisComponent {
    @Resource
    private RedisUtils redisUtils;

    public Long getUserHeartBeat(String userId)
    {
        //获取心跳
        return  (Long) redisUtils.get(Constants.REDIS_KEY_WS_USER_HEART_BEAT+userId);
    }

    public void saveTokenUserInfoDto(TokenUserInfoDto tokenUserInfoDto){
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken(),tokenUserInfoDto,Constants.REDIS_KEY_EXPIRES_DAY*2);

        //用userId再存一个token 便于通过id取DTO 通过DTO取token 因为聊天时取id
        redisUtils.setex(Constants.REDIS_KEY_WS_TOKEN_USERID+tokenUserInfoDto.getToken(),tokenUserInfoDto.getToken(),Constants.REDIS_KEY_EXPIRES_DAY*2);
    }

    public SysSettingDto getSysSetting(){
        SysSettingDto sysSettingDto = (SysSettingDto)redisUtils.get(Constants.REDIS_KEY_SYS_SETTING);
        //如果缓存中没有 就new一个 有默认值
        sysSettingDto = sysSettingDto ==null?new SysSettingDto():sysSettingDto;
        return sysSettingDto;
    }



}

