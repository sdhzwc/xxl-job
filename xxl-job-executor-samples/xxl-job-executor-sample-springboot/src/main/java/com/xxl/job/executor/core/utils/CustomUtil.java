package com.xxl.job.executor.core.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;


import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;


/**
 * 自定义工具类
 *
 * @author wuchao
 * @date 2024-07-25 17:15:20
 */
@Slf4j
@Component
public class CustomUtil {

    @Value("${spring.profiles.active}")
    private String springProfilesActive;
    @Resource
    private  StringRedisTemplate stringRedisTemplate;

    /**
     * 任务参数转换一
     *
     * @param jobParam 任务参数
     * @return {@link JSONObject}
     */
    public static JSONObject getJobParamJsonOne(String jobParam) {

        String jobHandleTime = DateUtil.format(new Date(), PURE_DATE_PATTERN);
        int redisTimeOut = 40;
        if (StrUtil.isNotBlank(jobParam)) {
            try {
                String[] arr = jobParam.split(",");
                boolean isNumber = NumberUtil.isNumber(arr[0]);
                if (isNumber){
                    DateTime dateTime = DateUtil.offsetMinute(new Date(), Integer.parseInt(arr[0]));
                    jobHandleTime = DateUtil.format(dateTime, PURE_DATE_PATTERN);
                }
                redisTimeOut = Integer.parseInt(arr[1]);
            } catch (Exception e) {
                XxlLog.error(log, "[CustomDateUtil][getJobParamJsonOne] 任务参数切割异常", e);
            }
        }
        JSONObject jobJson = new JSONObject();
        jobJson.put("jobHandleTime", jobHandleTime);
        jobJson.put("redisTimeOut", redisTimeOut);
        return jobJson;
    }



    /**
     * 任务参数转换
     *
     * @param jobParam 任务参数
     * @return {@link JSONObject}
     */
    public static JSONObject getJobParamJsonTwo(String jobParam) {

        JSONArray syncTypeList;
        JSONObject jsonParam;
        try {
            jsonParam = JSON.parseObject(jobParam);
            syncTypeList = jsonParam.getJSONArray("syncTypeList");
        } catch (Exception e) {
            syncTypeList = new JSONArray();
            XxlLog.error(log, StrUtil.format("参数有问题：{} 异常如下：", jobParam), e);
            jsonParam = new JSONObject();
        }

        String now = DateUtil.format(new Date(), PURE_DATE_PATTERN);
        XxlLog.info(log, StrUtil.format("数据计算时间:{}", now));

        List<JSONObject> jsonList = new ArrayList<>();
        if (syncTypeList.size() > 0) {
            syncTypeList.forEach(str -> {
                JSONObject json = JSON.parseObject(str.toString());
                // isDoubleCounting 是否重复计算 0-否 1-是
                Integer offset = json.getInteger("offset");
                Integer dateType = json.getInteger("dateType");
                String startTime = DateUtil.format(DateUtil.offsetDay(new Date(), -offset), PURE_DATE_PATTERN);
                String endTime = DateUtil.format(DateUtil.offsetDay(new Date(), -1), PURE_DATE_PATTERN);
                json.put("startTime", startTime);
                json.put("endTime", endTime);
                json.put("dateType", dateType);
                jsonList.add(json);
            });
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("list", jsonList);
        jsonObject.put("redisTimeOut", jsonParam.get("redisTimeOut"));
        return jsonObject;
    }


    /**
     * 任务上锁
     *
     * @param jobBeanName 任务Bean名称
     * @return {@link JSONObject }
     */
    public  JSONObject jobLock(String jobBeanName) {
        String jobParam = XxlJobHelper.getJobParam();
        JSONObject jobParamJson = CustomUtil.getJobParamJsonOne(jobParam);
        String key = jobBeanName + "_" + springProfilesActive;
        XxlLog.info(log, String.format("任务：%s 方法参数：%s", jobBeanName, jobParam));
        String value = stringRedisTemplate.opsForValue().get(key);
        XxlLog.info(log, String.format("任务：%s redis value值：%s", jobBeanName, value));
        if (StrUtil.isNotBlank(value)) {
            XxlLog.info(log, "任务：%s 正在进行中,请排队!");
        } else {
            jobParamJson.put("key", key);
            long redisTimeOut = jobParamJson.getLong("redisTimeOut");
            stringRedisTemplate.opsForValue().set(key, DateUtil.now(), redisTimeOut, TimeUnit.MINUTES);
        }
        return jobParamJson;
    }

    /**
     * 任务解锁
     *
     * @param jobBeanName 任务Bean名称
     */
    public  void jobUnlock(String jobBeanName) {
        String key = jobBeanName + "_" + springProfilesActive;
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取系统当前时间
     *
     * @return long
     */
    public long getSystemCurrentTime(){
        return System.currentTimeMillis();
    }

}
