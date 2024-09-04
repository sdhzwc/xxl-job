package com.xxl.job.executor.core.config.annotation;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * job param
 *
 * @author wuchao
 * @date 2024-08-13 13:11:47
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobParam {

    /**
     * job name
     *
     * @return {@link String }
     */
    String value();

    /**
     * time unit
     *
     * @return {@link TimeUnit }
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * redis time out timeUnit
     *
     * @return {@link TimeUnit }
     */
    TimeUnit redisTimeUnit() default TimeUnit.MINUTES;

}
