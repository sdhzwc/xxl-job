package com.xxl.job.executor.core.config.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * retry
 *
 * @author wuchao
 * @date 2024-09-04 17:02:47
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Retryable {

    /**
     * retry times
     *
     * @return int
     */
    int retryTimes() default 3;

    /**
     * retry interval
     *
     * @return int
     */
    int retryInterval() default 1;

    /**
     * time unit
     *
     * @return {@link TimeUnit }
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}

