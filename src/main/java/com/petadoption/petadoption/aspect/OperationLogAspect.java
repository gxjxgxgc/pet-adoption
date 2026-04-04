package com.petadoption.petadoption.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    // 定义切点，拦截所有controller包下的方法
    @Pointcut("execution(* com.petadoption.petadoption.controller.*.*(..))")
    public void operationLog() {}

    // 前置通知
    @Before("operationLog()")
    public void doBefore(JoinPoint joinPoint) {
        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            // 记录请求信息
            log.info("[操作日志] 请求时间: {}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("[操作日志] 请求地址: {}", request.getRequestURL().toString());
            log.info("[操作日志] 请求方式: {}", request.getMethod());
            log.info("[操作日志] 客户端IP: {}", request.getRemoteAddr());
            log.info("[操作日志] 类名方法: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
            log.info("[操作日志] 请求参数: {}", Arrays.toString(joinPoint.getArgs()));
        }
    }

    // 环绕通知
    @Around("operationLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        try {
            Object result = proceedingJoinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("[操作日志] 执行时间: {}ms", endTime - startTime);
            return result;
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("[操作日志] 执行异常: {}ms", endTime - startTime);
            log.error("[操作日志] 异常信息: {}", e.getMessage());
            throw e;
        }
    }

    // 后置通知
    @AfterReturning("operationLog()")
    public void doAfterReturning() {
        log.info("[操作日志] 请求处理完成");
    }

    // 异常通知
    @AfterThrowing(pointcut = "operationLog()", throwing = "e")
    public void doAfterThrowing(Throwable e) {
        log.error("[操作日志] 请求处理异常: {}", e.getMessage());
    }
}
