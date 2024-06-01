package com.xxl.job.executor.core.utils;

import com.xxl.job.core.context.XxlJobHelper;
import org.slf4j.Logger;

public class XxlLog {

    /**
     * 打印本地日志和定时任务日志。
     *
     * @param message 日志信息
     */
    public static void info(Logger logger, String message) {
        logger.info(message);
        XxlJobHelper.log(message);
    }

    /**
     * 打印本地日志和定时任务日志
     *
     * @param message   日志信息
     * @param throwable 错误信息
     */
    public static void error(Logger logger, String message, Throwable throwable) {
        logger.error(message, throwable);
        XxlJobHelper.log("定时任务异常：" + message);
        XxlJobHelper.log(throwable);
        XxlJobHelper.handleFail();
    }


}
