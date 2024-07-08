package com.easybbs.annotation;

import java.lang.annotation.*;

//定义注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GlobalInterceptor {
    boolean checkLogin() default true;

    boolean checkAdmin() default false; //超级管理员
}
