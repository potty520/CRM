package com.crm.aspect;

import com.crm.common.SecurityUtils;
import com.crm.entity.SysOperLog;
import com.crm.mapper.SysOperLogMapper;
import com.crm.security.LoginUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Aspect
@Component
public class OperLogAspect {

    private final SysOperLogMapper operLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OperLogAspect(SysOperLogMapper operLogMapper) {
        this.operLogMapper = operLogMapper;
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface OperLog {
        String module() default "";
        String operation() default "";
    }

    @Around("@annotation(operLog)")
    public Object around(ProceedingJoinPoint point, OperLog operLog) throws Throwable {
        Object result = point.proceed();
        try {
            LoginUser user = SecurityUtils.getLoginUser();
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes()).getRequest();
            SysOperLog log = new SysOperLog();
            log.setUserId(user.getUserId());
            log.setUsername(user.getUsername());
            log.setModule(operLog.module());
            log.setOperation(operLog.operation());
            log.setMethod(((MethodSignature) point.getSignature()).getMethod().getName());
            log.setParams(objectMapper.writeValueAsString(point.getArgs()));
            log.setIp(request.getRemoteAddr());
            operLogMapper.insert(log);
        } catch (Exception ignored) {
        }
        return result;
    }
}
