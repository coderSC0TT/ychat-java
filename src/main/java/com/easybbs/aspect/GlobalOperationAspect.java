package com.easybbs.aspect;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.exception.BusinessException;
import com.easybbs.redis.RedisUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @ClassName GlobalOperationAspect
 * @Description 全局操作切面
 * @Author Suguo
 * @LastChangeDate 2024/7/8 上午11:12
 */
@Aspect
@Component("GlobalOperationAspect")
public class GlobalOperationAspect {
    @Resource
    private RedisUtils redisUtils;

    private  static  final Logger logger = LoggerFactory.getLogger(GlobalOperationAspect.class);

    //前置通知
    @Before("@annotation(com.easybbs.annotation.GlobalInterceptor)") //被注解的方法执行之前执行切面方法
    public void interceptorDo(JoinPoint point) {
        try {
            Method method = ((MethodSignature) point.getSignature()).getMethod();
            GlobalInterceptor interceptor = method.getAnnotation(GlobalInterceptor.class);
            if (null == interceptor) return;
            //如果注解配置了需要检查登录或管理员权限,则调用checkLogin()方法进行检查
            if (interceptor.checkLogin() || interceptor.checkAdmin()) {
                checkLogin(interceptor.checkAdmin());
            }
        } catch (BusinessException e) {
            logger.error("全局拦截异常", e);
            throw e;
        } catch (Exception e) {
            logger.error("全局拦截异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);
        } catch (Throwable e) {
            logger.error("全局拦截异常", e);
            throw new BusinessException(ResponseCodeEnum.CODE_500);

        }
    }

    private void checkLogin(Boolean checkAdmin){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token =request.getHeader("token");
        TokenUserInfoDto tokenUserInfoDto =(TokenUserInfoDto) redisUtils.get(Constants.REDIS_KEY_WS_TOKEN+token);
        if(null==tokenUserInfoDto){
            throw new BusinessException(ResponseCodeEnum.CODE_901);
        }
        //如果需要管理员权限 无权限不能请求对应接口
        if(checkAdmin && !tokenUserInfoDto.getAdmin()){
            throw  new BusinessException(ResponseCodeEnum.CODE_404);
        }

    }
}

