package com.jiangnan.travel.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SlowQueryAspect {

    private static final long SLOW_THRESHOLD_MS = 500;

    @Around("execution(public * com.jiangnan.travel.service.impl.*ServiceImpl.*(..))")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return point.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost > SLOW_THRESHOLD_MS) {
                MethodSignature signature = (MethodSignature) point.getSignature();
                String className = signature.getDeclaringTypeName();
                String methodName = signature.getName();
                log.warn("[慢查询监控] {}.{} 执行耗时 {}ms，超过阈值 {}ms", className, methodName, cost, SLOW_THRESHOLD_MS);
            }
        }
    }
}
