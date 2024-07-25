package com.xxl.job.executor.task;

import com.alibaba.fastjson2.JSONObject;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.executor.core.utils.CustomUtil;
import com.xxl.job.executor.core.utils.XxlLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * 测试作业
 *
 * @author wuchao
 * @date 2024-07-25 17:49:38
 */
@Slf4j
@RestController
public class TestXxlJob {

    @Resource
    private CustomUtil customUtil;

    /**
     * 慢病毛利额
     */
    @XxlJob("testJob")
    @RequestMapping("testJob")
    public void testJob() {
        long startTime = customUtil.getSystemCurrentTime();
        String jobBeanName = "testJob";
        try {
            JSONObject jobParamJson = customUtil.jobLock(jobBeanName);
            // job逻辑处理
            test(jobParamJson);
            customUtil.jobUnlock(jobBeanName);
            XxlLog.info(log, String.format("任务：%s redis已清理!!!", jobBeanName));
        } catch (Exception e) {
            customUtil.jobUnlock(jobBeanName);
            XxlLog.error(log, String.format("任务：%s 同步异常: ", jobBeanName), e);
        }
        long endTime = customUtil.getSystemCurrentTime();
        XxlLog.info(log, String.format("任务：%s 同步完成,耗时: %dms", jobBeanName, (endTime - startTime)));
    }


    public void test(JSONObject jobParamJson) {
        String jobHandleTime = jobParamJson.getString("jobHandleTime");
        XxlLog.info(log, String.format("任务执行时间：%s", jobHandleTime));
    }
}
