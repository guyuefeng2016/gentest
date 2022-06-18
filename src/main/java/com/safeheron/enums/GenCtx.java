package com.safeheron.enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: 类和方法都有该注解时，方法注解优先级高
 * @author: guyuefeng
 * @create: 2022-06-14 18:55
 **/
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenCtx {

    /**
     * 是否跳过对当前方法的测试, 如果写在类上面，则跳过对当前类的测试
     * @return
     */
    boolean skip() default false;

    /**
     * 是否跳过对private方法的测试
     * @return
     */
    boolean skipPrivate() default false;

    /**
     * 是否跳过所有非public方法的测试
     * @return
     */
    boolean skipNotPublic() default false;

    /**
     * 只测试某些方法，填方法名，默认不填测试所有方法
     * @return
     */
    String[] testOnlyMethod() default {};

    /**
     * 不测试某些方法，填方法名，默认不填不排除任何方法
     * @return
     */
    String[] testExcludeMethod() default {};

    /**
     * 是否只打印error错误
     * @return
     */
    boolean logOnlyErr() default false;

    /**
     * 是否打印性能报告
     * @return
     */
    boolean logPerformance() default true;

    /**
     * 打印优先级，值越大，越往后打印 ，只支持类级别
     * @return
     */
    int printPriority() default Integer.MAX_VALUE;

    /**
     * 是否开启多线程执行，属性仅支持方法级别
     * @return
     */
    boolean enableThread() default false;

    /**
     * 线程个数
     * @return
     */
    int threadCount() default 1;

    /**
     * 执行次数
     * @return
     */
    int executeCount() default 1;
}
