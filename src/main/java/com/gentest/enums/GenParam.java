package com.gentest.enums;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-14 18:55
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GenParam {

    /**
     * 如果是普通类型，直接填值即可 【如果要测试某个字段的多个值：以$分割，这几个候选值将会以等概率形式进行测试 ，如: 12$13$14 , 12和13和14将会以等概率赋值，每次测试只会选择其中一个】
     *
     * 如果是数组，以英文的逗号分割 【如果要测试某个字段的多个值：以$分割，如: 2,3,4$4,5,6$7,8,9，这几个候选值将会以等概率形式进行测试 ,  如果要测试多组，多组之间以#分割】
     *
     * 如果是Map，请保持正确的json格式，如： {"name":"张三","age":18} 【如果要测试某个字段的多个值：以$分割， 如{"name":"张三","age":"18$19$20"}，这几个候选值将会以等概率形式进行测试
     *  如果要测试多组，多组之间以#分割，如{"name":"张三","age":"18$19$20"}#{"name":"李四","age":"18$19$20"}，那么这两个map将以等概率选择其中一个，age属性也将等概率选择】
     *
     * 如果是实体类型，需要修改某些字段，请保持正确的json格式，如  {"name":"张三","age":18} 【如果要测试某个字段的多个值：以$分割，如{"name":"张三","age":"18$19$20"}，
     * 这几个候选值将会以等概率形式进行测试，如果要测试多组，多组之间以#分割，如{"name":"张三","age":"18$19$20"}#{"name":"李四","age":"18$19$20"}，那么这两个map将以等概率选择其中一个，
     * age属性也将等概率选择】
     * @return
     */
    String value() default "";

    /**
     * argIndex表示第几个参数，如果写全所有参数，则这个参数可以忽略，否则需要, 参数默认从 argIndex=0开始
     * @return
     */
    int argIndex() default 0;

    /**
     * 是否跳过对当前方法的测试
     * @return
     */
    boolean skip() default false;

    /**
     * 是否只打印error错误
     * @return
     */
    boolean logErr() default false;

    /**
     * 是否打印性能报告
     * @return
     */
    boolean logPerformance() default false;
}
