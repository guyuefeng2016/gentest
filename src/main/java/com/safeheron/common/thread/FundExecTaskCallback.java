package com.safeheron.common.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public abstract class FundExecTaskCallback<T> implements Callable<T> {
    
private static final Logger logger = LoggerFactory.getLogger(FundExecTaskCallback.class);
    private String taskName;
    
    public abstract T onExec() throws Exception;

    @Override
    public T call() throws Exception {
        long startTime = System.currentTimeMillis();
        logger.info("Task {} started", taskName);
        try {
            T rst = onExec();
            logger.info("Task {} exec succ, time {}ms", taskName, System.currentTimeMillis()-startTime);
            return rst;
        } catch(Exception e) {
            logger.error("Task {} exec failed, time {}ms", taskName, System.currentTimeMillis()-startTime,e);
            throw e;
        }
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

}
