package com.xxl.job.executor.core.utils;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        String jobHandleTime;
        int offset = -30;
        int redisTimeOut = 40;
        if (StrUtil.isNotBlank(jobParam) && jobParam.contains(",")) {
            String[] arr = jobParam.split(",");
            String offsetOrDate = arr[0];
            // offset or date
            try {
                DateTime dateTime = DateUtil.parse(offsetOrDate, PURE_DATE_PATTERN);
                jobHandleTime = DateUtil.format(dateTime, PURE_DATE_PATTERN);
            } catch (Exception e) {
                DateTime dateTime = DateUtil.offsetMinute(new Date(), Integer.parseInt(offsetOrDate));
                jobHandleTime = DateUtil.format(dateTime, PURE_DATE_PATTERN);
            }
            // redis lock time
            try {
                redisTimeOut = Integer.parseInt(arr[1]);
            } catch (Exception ignored) {
                // default lock time
            }
        } else {
            DateTime dateTime = DateUtil.offsetMinute(new Date(), offset);
            jobHandleTime = DateUtil.format(dateTime, PURE_DATE_PATTERN);
        }
        JSONObject jobJson = new JSONObject();
        jobJson.put("jobHandleTime", jobHandleTime);
        jobJson.put("redisTimeOut", redisTimeOut);
        return jobJson;
    }

    /**
     * 获取任务参数
     *
     * @param jobParamKey jobParamKey
     * @return {@link String }
     */
    public static String getXxlJobParam(String jobParamKey) {

        String jobParam = XxlJobHelper.getJobParam();
        JSONObject jobJson = new JSONObject();
        if (StrUtil.isNotBlank(jobParam)) {
            final String xxlSeparator = "XXL";
            List<String> paramList = StrUtil.split(jobParam, xxlSeparator);
            jobJson.put("jobParam", paramList.get(0));
            String complementDataTime = paramList.get(1);
            if (StrUtil.isNotBlank(complementDataTime)) {
                String dateSeparator = " - ";
                List<String> complementDataTimeList = StrUtil.split(complementDataTime, dateSeparator);
                DateTime beginDate = DateUtil.parse(complementDataTimeList.get(0));
                DateTime endDate = DateUtil.parse(complementDataTimeList.get(1));
                long dayLength = DateUtil.betweenDay(beginDate, endDate, true);
                List<String> complementDateList = new ArrayList<>();
                for (long i = 0; i < (dayLength + 1); i++) {
                    String dateStr = DateUtil.format(DateUtil.offsetDay(beginDate, (int) i), PURE_DATE_PATTERN);
                    complementDateList.add(dateStr);
                }
                jobJson.put("complementDateList", complementDateList);
            }
        }
        return jobJson.getString(jobParamKey);
    }

}
