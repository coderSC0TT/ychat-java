package com.easybbs.controller;

import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.entity.vo.UserInfoVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.redis.RedisUtils;
import com.easybbs.service.UserInfoService;
import com.easybbs.utils.CopyTools;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @ClassName AccountController
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/3 下午9:15
 */
@RestController("accountController")
@RequestMapping("/account")
@Validated
public class AccountController extends  ABaseController{

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private UserInfoService userInfoService;

    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100, 42);
        String code = captcha.text();
        String checkCodeBase64 = captcha.toBase64();
        String checkCodeKey = UUID.randomUUID().toString();
        redisUtils.setex(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey,code,Constants.REDIS_KEY_EXPIRES_ONE_MIN);
        Map<String,String> result=new HashMap<>();
        result.put("checkCode",checkCodeBase64);
        result.put("checkCodeKey",checkCodeKey);
        return  getSuccessResponseVO(result);
    }

    @RequestMapping("/register")
    public ResponseVO register(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String nickName,
                               @NotEmpty String checkCode){
        try{
            if(!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey)))
                throw new BusinessException("图片验证码不正确");
            userInfoService.register(email, nickName, password);
            return getSuccessResponseVO(null);
        }finally {
            //如果错误 删除缓存防止重试
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }

    }

    @RequestMapping("/login")
    public ResponseVO login(@NotEmpty String checkCodeKey,
                               @NotEmpty @Email String email,
                               @NotEmpty String password,
                               @NotEmpty String checkCode){
        try{
            if(!checkCode.equalsIgnoreCase((String) redisUtils.get(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey)))
                throw new BusinessException("图片验证码不正确");
            UserInfoVO userInfoVO=userInfoService.login(email, password);
            return getSuccessResponseVO(userInfoVO);
        }finally {
            redisUtils.delete(Constants.REDIS_KEY_CHECK_CODE+checkCodeKey);
        }

    }
}

