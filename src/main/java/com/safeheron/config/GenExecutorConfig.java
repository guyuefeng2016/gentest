package com.safeheron.config;

import com.safeheron.common.thread.GenTaskExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @program: parent
 * @description:
 * @author: guyuefeng
 * @create: 2020-04-17 17:29
 **/
@Configuration
public class GenExecutorConfig {


    @Bean
    public GenTaskExecutor genTaskExecutor(){
        GenTaskExecutor genTaskExecutor = new GenTaskExecutor();
        genTaskExecutor.setName("gen-thread");
        genTaskExecutor.setMaxPoolSize(1024);
        return genTaskExecutor;
    }
}
