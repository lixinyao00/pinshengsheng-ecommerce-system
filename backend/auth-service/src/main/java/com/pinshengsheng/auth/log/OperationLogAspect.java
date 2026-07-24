package com.pinshengsheng.auth.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// 声明这是一个 AOP 切面类
@Aspect
@Component
public class OperationLogAspect {

    // 使用 Spring Boot 默认日志工具，将日志打印到控制台
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    // 拦截所有标记了 @OperationLog 的方法
    @Around("@annotation(operationLog)")
    public Object recordOperation(ProceedingJoinPoint joinPoint,
                                  OperationLog operationLog) throws Throwable {

        // 记录接口开始执行的时间，用于计算耗时
        long startTime = System.currentTimeMillis();

        try {
            // 执行真正的 Controller 方法，例如登录方法
            Object result = joinPoint.proceed();

            // 方法正常结束后，打印成功日志
            long costTime = System.currentTimeMillis() - startTime;
            log.info("操作日志 | 操作={} | 方法={} | 结果=成功 | 耗时={}ms",
                    operationLog.value(),
                    joinPoint.getSignature().toShortString(),
                    costTime);

            return result;
        } catch (Throwable throwable) {
            // 方法执行异常时，记录失败日志后继续把异常抛出去
            log.error("操作日志 | 操作={} | 方法={} | 结果=失败",
                    operationLog.value(),
                    joinPoint.getSignature().toShortString());

            throw throwable;
        }
    }
}
