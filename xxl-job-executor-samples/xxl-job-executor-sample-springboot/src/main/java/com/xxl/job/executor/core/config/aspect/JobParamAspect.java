package com.xxl.job.executor.core.config.aspect;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.executor.core.config.annotation.JobParam;
import com.xxl.job.executor.core.utils.CustomUtil;
import com.xxl.job.executor.core.utils.XxlLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;


/**
 * job param aspect
 *
 * @author wuchao
 * @date 2024-08-13 14:01:05
 */
@Aspect
@Component
@Slf4j
public class JobParamAspect {

    @Value("${spring.profiles.active}")
    private String springProfilesActive;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * job param point cut
     */
    @Pointcut("@annotation(com.xxl.job.executor.core.config.annotation.JobParam)")
    public void jobParamPointCut() {
    }

    /**
     * before
     *
     * @param joinPoint joinPoint
     */
    public String before(JoinPoint joinPoint) {
        String jobName = getJobName(joinPoint);
        String jobParam = CustomUtil.getXxlJobParam("jobParam");
        JSONObject jobParamJson = CustomUtil.getJobParamJsonOne(jobParam);
        String key = jobName + "_" + springProfilesActive;
        XxlLog.info(log, String.format("job name：%s , job param：%s", jobName, jobParam));
        String value = stringRedisTemplate.opsForValue().get(key);
        XxlLog.info(log, String.format("job name：%s , redis value：%s", jobName, value));
        if (StrUtil.isNotBlank(value)) {
            XxlLog.info(log, String.format("job name：%s in progress, please stand in line!", jobName));
            return value;
        }
        jobParamJson.put("key", key);
        long redisTimeOut = jobParamJson.getLong("redisTimeOut");
        stringRedisTemplate.opsForValue().set(key, DateUtil.now(), redisTimeOut, getRedisTimeUnit(joinPoint));
        return null;
    }

    /**
     * after
     *
     * @param joinPoint joinPoint
     */
    @AfterReturning(value = "jobParamPointCut()")
    public void afterReturning(JoinPoint joinPoint) {
        String jobName = getJobName(joinPoint);
        String key = jobName + "_" + springProfilesActive;
        stringRedisTemplate.delete(key);
        XxlLog.info(log, String.format("job name：%s , redis cleaned!", jobName));
    }

    /**
     * after throwing
     *
     * @param joinPoint joinPoint
     */
    @AfterThrowing(value = "jobParamPointCut()", throwing = "e")
    public void afterThrowing(JoinPoint joinPoint, Exception e) {
        String jobName = getJobName(joinPoint);
        String key = jobName + "_" + springProfilesActive;
        stringRedisTemplate.delete(key);
        XxlLog.error(log, String.format("job name：%s , sync exception: ", jobName), e);
    }

    /**
     * job execution time
     *
     * @param joinPoint joinPoint
     * @return {@link Object }
     * @throws Throwable Throwable
     */
    @Around(value = "jobParamPointCut()")
    public Object jobExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = getJobName(joinPoint);
        long start = System.currentTimeMillis();
        String before = before(joinPoint);
        Object proceed = null;
        if (StrUtil.isBlank(before)) {
            proceed = joinPoint.proceed();
        }
        long executionTime = System.currentTimeMillis() - start;
        XxlLog.info(log, String.format("job name：%s , executed in %s ms ", methodName, executionTime));
        return proceed;
    }

    /**
     * get job param
     *
     * @param joinPoint joinPoint
     * @return {@link JobParam }
     */
    private JobParam getJobParam(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        return method == null ? null : method.getAnnotation(JobParam.class);
    }

    /**
     * get job name
     *
     * @param joinPoint joinPoint
     * @return {@link String }
     */
    private String getJobName(JoinPoint joinPoint) {
        JobParam jobParam = getJobParam(joinPoint);
        return jobParam == null ? null : jobParam.value();
    }

    /**
     * get redis time out timeUnit
     *
     * @param joinPoint joinPoint
     * @return {@link String }
     */
    private TimeUnit getRedisTimeUnit(JoinPoint joinPoint) {
        JobParam jobParam = getJobParam(joinPoint);
        return jobParam == null ? TimeUnit.MINUTES : jobParam.redisTimeUnit();
    }

}
