package com.gentest.gen;

import com.gentest.common.GenSpringContextHolder;
import com.gentest.gencode.CaseInput;
import com.gentest.gencode.GenerateFileService;
import com.gentest.gencode.TestCaseClassInfo;
import com.gentest.report.GenReport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: guyuefeng
 * @Date: 2022/6/23
 */
@Slf4j
public class AbstractGenTestCase {

    /**
     *
     * @param sourceCodeMap
     * @param typeStr
     * @param v
     * @param argIndex
     */
    protected static void conveterSimpleTypeSourceCode(Map<Integer, StringBuilder> sourceCodeMap, String typeStr, Object v, AtomicInteger argIndex){
        if (sourceCodeMap == null){
            return;
        }
        StringBuilder currentSourceCode = new StringBuilder();
        int currentSouceCodeIndex = sourceCodeMap.size();
        currentSourceCode.append(typeStr).append(" var").append(argIndex.get()).append(currentSouceCodeIndex).append(" = ").append(v).append(";");
        sourceCodeMap.put(currentSouceCodeIndex, currentSourceCode);
    }


    /**
     *
     * @param resultBuilder
     * @param v
     */
    protected static void conveterSimpleResultSouceCode(StringBuilder resultBuilder, Object v){
        if (resultBuilder == null){
            return;
        }
        resultBuilder.append(v);
    }


    /**
     *
     * @param fieldName
     * @param sourceCodeMap
     * @param localBeanIndex
     * @param resultBuilder
     * @param argIndex
     */
    protected static void setBeanFiledSourceCode(String fieldName, Map<Integer, StringBuilder> sourceCodeMap, Integer localBeanIndex, StringBuilder resultBuilder, AtomicInteger argIndex){
        String setName = fieldName.substring(0,1).toUpperCase().concat(fieldName.substring(1));
        Integer propertyIndex = sourceCodeMap.size();
        StringBuilder currentSourceCode = new StringBuilder();
        currentSourceCode.append("var").append(argIndex.get()).append(localBeanIndex).append(".set").append(setName).append("(").append(resultBuilder.toString()).append(");");;
        sourceCodeMap.put(propertyIndex, currentSourceCode);
    }


    /**
     * 生成测试用例
     * @param generateCase
     * @param testCaseInfoList
     * @param caseInput
     */
    protected static void generateTestCase(Boolean generateCase, List<TestCaseClassInfo> testCaseInfoList, CaseInput caseInput, Class applicationClass){
        if (generateCase != null && generateCase){
            GenerateFileService generateFileService = GenSpringContextHolder.getBean(GenerateFileService.class);
            generateFileService.genTestCase(testCaseInfoList, caseInput, applicationClass);
        }
    }

    /**
     * 打印测试用例
     */
    protected static void printTestCase(){
        Queue<String> queue = GenReport.printReport(false);
        if (!CollectionUtils.isEmpty(queue)) {
            boolean dingPush = false;
            //GenDingPush dingDingPush = null;
            if (dingPush){
                //dingDingPush = GenSpringContextHolder.getBean(GenDingPush.class);
            }
            StringBuilder dingSb = new StringBuilder();

            while (queue.size() > 0) {
                dingSb.append(queue.poll());
            }
            while (dingSb.length() > 0) {
                if (dingSb.length() > 4000) {
                    String dingText = dingSb.substring(0, 4000);
                    if (dingPush) {
                        //dingDingPush.sendPush(dingText);
                    } else {
                        System.out.println(dingText);
                    }
                    dingSb.delete(0, 4000);
                } else {
                    if (dingPush) {
                        //dingDingPush.sendPush(dingSb.toString());
                    } else {
                        System.out.println(dingSb.toString());
                    }
                    dingSb.delete(0, dingSb.length());
                }
                try {
                    if (dingPush){
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     *
     * @param configurableApplicationContext
     */
    protected static void exit(ConfigurableApplicationContext configurableApplicationContext){
        int exitCode = SpringApplication.exit(configurableApplicationContext, (ExitCodeGenerator) () -> 0);
        System.exit(exitCode);
    }
}
