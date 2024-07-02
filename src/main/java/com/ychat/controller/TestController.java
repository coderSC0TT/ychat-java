package com.ychat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName TestController
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/1 下午8:51
 */
@RestController
public class TestController {
    @RequestMapping("/test")
    public String test() {
        return "test";
    }
}

