package com.easybbs.controller;

import com.easybbs.entity.vo.ResponseVO;
import com.wf.captcha.ArithmeticCaptcha;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

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
    @RequestMapping("/checkCode")
    public ResponseVO checkCode(){
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(130, 45);
        String code = captcha.text();
        String checkCodeBase64 = captcha.toBase64();
        Map<String,String> result=new HashMap<>();
        result.put("checkCode",checkCodeBase64);

        return  getSuccessResponseVO(result);
    }
}

