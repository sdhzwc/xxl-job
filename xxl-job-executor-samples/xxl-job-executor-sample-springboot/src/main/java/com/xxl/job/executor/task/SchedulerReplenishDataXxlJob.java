package com.xxl.job.executor.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.executor.XxlJobExecutor;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.executor.core.utils.XxlLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static cn.hutool.core.date.DatePattern.PURE_DATE_PATTERN;
import static cn.hutool.core.date.DatePattern.PURE_TIME_PATTERN;

/**
 * 调度程序补充数据 XXL作业
 *
 * @author 28170
 * @date 2024/06/01
 */
@Component
@Slf4j
public class SchedulerReplenishDataXxlJob {


    /**
     * 补数任务
     *
     * @throws Exception 例外
     */
    @XxlJob("schedulerReplenishHandler")
    public void schedulerReplenishHandler() throws Exception {

        String jobParam = XxlJobHelper.getJobParam();
        if (StrUtil.isBlank(jobParam)) {
            XxlLog.info(log, "-------- 补数程序的参数不可以为空 ---------");
            return;
        }
        String[] arr = jobParam.split("#");
        if (arr.length != 5) {
            XxlLog.info(log, "-------- 程序缺少必要参数,请检查填写是否正确！！！ ---------");
        }
        // bean名称#[开始时间,结束时间]#推算时间用第几个字段#参数#传递具体日期or偏移量方式  例如：taskJob#[20230101]#2#a,-22,4#1
        String param_1 = arr[0]; // bean名称
        String param_2 = arr[1]; // [开始时间,结束时间] 例如[20230101],[20230101,20230304],[20230101~20230105]
        int param_3 = Integer.parseInt(arr[2]); // 推算时间用第几个字段
        String param_4 = arr[3]; // 参数
        String param_5 = arr[4]; // 1-日期 2-偏移量

        XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
        if (xxlJobContext == null) {
            return;
        }
        long jobId = xxlJobContext.getJobId();
        String jobLogFileName = xxlJobContext.getJobLogFileName();
        int shardIndex = xxlJobContext.getShardIndex();
        int shardTotal = xxlJobContext.getShardTotal();
        // 获取执行的任务
        IJobHandler iJobHandler = XxlJobExecutor.loadJobHandler(param_1);

        String dateReplace = param_2.replace("]", "").replace("[", "");
        boolean dateContains = dateReplace.contains("~");
        List<String> dateList = new ArrayList<>();
        Date now = new Date();
        if (dateContains) {
            String[] dateSplit = dateReplace.split("~");
            DateTime beginDate = DateUtil.parse(dateSplit[0]);
            DateTime endDate = DateUtil.parse(dateSplit[1]);
            long dayLength = DateUtil.betweenDay(beginDate, endDate, true);
            for (long i = 0; i < (dayLength + 1); i++) {
                String dateStr = DateUtil.format(DateUtil.offsetDay(beginDate, (int) i), PURE_DATE_PATTERN);
                long offset = betweenMinute(now, dateStr);
                String[] paramArr = param_4.split(",");
                if ("1".equals(param_5)){
                    paramArr[param_3 - 1] = String.format("%s", dateStr);
                } else {
                    paramArr[param_3 - 1] = String.format("-%s", offset);
                }
                dateList.add(StrUtil.join(",", Arrays.asList(paramArr)));
            }
        } else {
            String[] dateSplit = dateReplace.split(",");
            for (String dateStr : dateSplit) {
                long offset = betweenMinute(now, dateStr);
                String[] paramArr = param_4.split(",");
                if ("1".equals(param_5)){
                    paramArr[param_3 - 1] = String.format("%s", dateStr);
                } else {
                    paramArr[param_3 - 1] = String.format("-%s", offset);
                }
                dateList.add(StrUtil.join(",", Arrays.asList(paramArr)));
            }
        }

        // 执行补数操作
        for (String taskJobParam : dateList) {
            XxlJobContext taskJobContext = new XxlJobContext(jobId, taskJobParam, jobLogFileName, shardIndex, shardTotal);
            XxlJobContext.setXxlJobContext(taskJobContext);
            iJobHandler.execute();
        }
    }

    public static long betweenMinute(Date now, String dateStr) {
        String format = String.format("%s%s", dateStr, DateUtil.format(now, PURE_TIME_PATTERN));
        DateTime startDate = DateUtil.parse(format);
        return DateUtil.between(startDate, now, DateUnit.MINUTE);
    }


    /**
     * 正常任务
     */
    @XxlJob("taskJob")
    public void taskJob() {

        String jobParam = XxlJobHelper.getJobParam();
        XxlLog.info(log, String.format("-------- 正常任务参数：%s", jobParam));

        assert jobParam != null;
        String[] arr = jobParam.split(",");
        DateTime jobHandleDate = DateUtil.offsetMinute(new Date(), Integer.parseInt(arr[1]));
        XxlLog.info(log, String.format("-------- 正常任务补数时间：%s", jobHandleDate));
    }

}
