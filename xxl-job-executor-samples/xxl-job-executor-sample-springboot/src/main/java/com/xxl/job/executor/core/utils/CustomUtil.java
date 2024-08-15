package com.xxl.job.executor.core.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import java.util.Date;
import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;


/**
 * 自定义工具类
 *
 * @author wuchao
 * @date 2024-07-25 17:15:20
 */
@Slf4j
public class CustomUtil {

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
                if (arr.length > 1) {
                    redisTimeOut = Integer.parseInt(arr[1]);
                }
            } catch (Exception e) {
                XxlLog.error(log, "[CustomUtil][getJobParamJsonOne] job param split exception：", e);
            }
        }
        JSONObject jobJson = new JSONObject();
        jobJson.put("jobHandleTime", jobHandleTime);
        jobJson.put("redisTimeOut", redisTimeOut);
        return jobJson;
    }

}
