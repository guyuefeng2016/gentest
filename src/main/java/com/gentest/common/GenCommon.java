package com.gentest.common;

import lombok.extern.slf4j.Slf4j;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-17 18:30
 **/
@Slf4j
public class GenCommon {

    /**
     * info消息
     * @param logOnlyErrorFlag
     * @param message
     * @param args
     */
    public static void info(Boolean logOnlyErrorFlag, String message, Object... args){
        if (logOnlyErrorFlag){
            return;
        }
        String str = String.format("gen-test-info:  %s",message);
        log.info(str,args);
    }

    /**
     * error消息
     * @param message
     * @param args
     */
    public static void error(String message, Object... args){
        String str = String.format("gen-test-error:  %s",message);
        log.error(str,args);
    }

}
