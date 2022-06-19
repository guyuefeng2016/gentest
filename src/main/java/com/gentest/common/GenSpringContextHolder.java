package com.gentest.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * @program: parent
 * @description:
 * @author: guyuefeng
 * @create: 2020-04-24 14:22
 **/
@Slf4j
@Configuration
public class GenSpringContextHolder implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;


    public static ApplicationContext getApplicationContext() {
        assertContextInjected();
        return applicationContext;
    }

    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }

    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }

    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext is null");
        }
    }

    public static void clearHolder() {
        log.debug("set ApplicationContext is null, current ApplicationContext:" + applicationContext);
        applicationContext = null;
    }

    public String getActiveProfile(){
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }

    @Override
    public void destroy() {
        GenSpringContextHolder.clearHolder();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (GenSpringContextHolder.applicationContext != null) {
            log.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:" + GenSpringContextHolder.applicationContext);
        }
        GenSpringContextHolder.applicationContext = applicationContext;
    }
}
