package com.xxl.job.executor.task;

import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.executor.core.config.annotation.JobLog;
import com.xxl.job.executor.core.utils.CustomUtil;
import com.xxl.job.executor.core.utils.XxlLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 测试作业
 *
 * @author wuchao
 * @date 2024-07-25 17:49:38
 */
@Slf4j
@RestController
public class TestXxlJob {

    /**
     * 测试job
     */
    @XxlJob("testJob")
    @RequestMapping("testJob")
    @JobLog("testJob")
    public void testJob() {

        try {
            JSONObject jobParamJson = CustomUtil.getJobParamJsonOne(XxlJobHelper.getJobParam());
            if ("20240812".equals(jobParamJson.getString("jobHandleTime"))) {
                System.out.println(1 / 0);
            }
            // job逻辑处理
            test(jobParamJson);
        } catch (Exception e) {
            XxlLog.error(log, String.format("job name：%s , sync exception: ", "testJob"), e);
        }
    }


    public void test(JSONObject jobParamJson) {
        String jobHandleTime = jobParamJson.getString("jobHandleTime");
        XxlLog.info(log, String.format("任务执行时间：%s", jobHandleTime));
    }
}
