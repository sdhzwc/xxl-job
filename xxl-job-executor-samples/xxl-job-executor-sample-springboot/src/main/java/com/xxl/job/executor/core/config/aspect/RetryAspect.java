package com.xxl.job.executor.core.config.aspect;

import cn.hutool.core.thread.ThreadUtil;
import com.xxl.job.executor.core.config.annotation.Retryable;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * retry aspect
 *
 * @author wuchao
 * @date 2024/09/04
 */
@Slf4j
@Aspect
@Component
public class RetryAspect {

    /**
     * retry point cut
     */
    @Pointcut("@annotation(com.xxl.job.executor.core.config.annotation.Retryable)")
    private void retryPointCut() {
    }

    /**
     * retry
     *
     * @param joinPoint join point
     * @return {@link Object }
     * @throws Exception exception
     */
    @Around("retryPointCut()")
    public Object retry(ProceedingJoinPoint joinPoint) throws Exception {
        Retryable retry = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Retryable.class);
        int maxRetryTimes = retry.retryTimes();
        int retryInterval = retry.retryInterval();
        TimeUnit timeUnit = retry.timeUnit();

        Throwable error = new RuntimeException();
        for (int retryTimes = 1; retryTimes <= maxRetryTimes; retryTimes++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable throwable) {
                error = throwable;
                log.warn("program exception, start retrying, retryTimes: {}", retryTimes);
            }
            ThreadUtil.sleep(retryInterval, timeUnit);
        }
        throw new Exception("the number of retries is exhausted", error);
    }
}
