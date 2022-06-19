package com.gentest.report;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-17 18:39
 **/
@Slf4j
public class GenReport {
    private static final AtomicInteger PERFORMANCE_COUNT = new AtomicInteger();
    private static  final AtomicInteger ERROR_COUNT = new AtomicInteger();
    private static final AtomicInteger INFO_COUNT = new AtomicInteger();
    private static final Stack<GenReportData> queueList = new Stack<>();
    public static final PriorityQueue<GenReportData> priorityQueue = new PriorityQueue<>((Comparator.comparingInt(o -> o.reportClassPriority)));

    /**
     * 打印报告
     */
    public static Queue<String> printReport(boolean printFlag){
        Queue<String> queue = new LinkedList<>();
        int size = priorityQueue.size();
        if (size > 0){
            StringBuilder sb = new StringBuilder();
            String line1 = "\n##############################################################################     自动化黑盒测试报告开始生成     ##############################################################################\n";
            String line2 = "本次共测试 " + size + " 个类：\tinfo报告：" + (INFO_COUNT.get() >> 1) + "例 \terror报告：" + ERROR_COUNT.get() + "例 \t性能报告：" + PERFORMANCE_COUNT.get() + "例\n详情如下: \n";
            sb.append(line1);
            sb.append(line2);
            if (printFlag) {
                System.out.println(line1);
                System.out.println(line2);
            }
            queue.add(sb.toString());
        }
        int methodIndex = 1;
        while (priorityQueue.size() > 0){
            StringBuilder lineSb = new StringBuilder();
            GenReportData genReportData = priorityQueue.poll();

            StringBuffer extraReport = genReportData.getExtraReport();
            StringBuffer errorReport = genReportData.getErrorReport();
            StringBuffer infoReport = genReportData.getInfoReport();
            StringBuffer performanceReport = genReportData.getPerformanceReport();
            AtomicInteger infoCount = genReportData.getInfoCount();
            AtomicInteger errorCount = genReportData.getErrorCount();
            AtomicInteger performanceCount = genReportData.getPerformanceCount();

            if (extraReport.length() > 0){
                String line3 = "（ " + methodIndex++ + " ） " + extraReport.toString() + "\n";
                lineSb.append(line3);
                if (printFlag) {
                    System.out.println(line3);
                }
            }
            String line4 = "当前类共有：\tinfo报告："+ (infoCount.get() >> 1) +"例 \terror报告："+ errorCount.get()+"例 \t性能报告："+ performanceCount.get()+"例\n";
            lineSb.append(line4);
            if (printFlag) {
                System.out.println(line4);
            }
            if (infoReport.length() > 0){
                String line5 = "¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ info报告 : ¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥\n当前类共有"+ (infoCount.get() >> 1) +"个方法执行调用：\n"+infoReport.toString()+"\n";
                lineSb.append(line5);
                if (printFlag) {
                    System.out.println(line5);
                }
            }
            if (errorReport.length() > 0){
                String line6 = "¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ error报告 : ¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥\n当前类共有"+ errorCount.get()+"个方法执行调用：\n"+errorReport.toString();
                lineSb.append(line6);
                if (printFlag) {
                    System.out.println(line6);
                }
            }
            if (performanceReport.length() > 0){
                String line7 = "¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥ 性能报告: ¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥¥\n当前类共有"+ performanceCount.get()+"例性能监控：\n"+performanceReport.toString()+"\n";
                lineSb.append(line7);
                if (printFlag) {
                    System.out.print(line7);
                }
            }
            if (lineSb.length() > 0) {
                queue.add(lineSb.toString());
            }
        }
        if (size > 0){
            String line8 = "############################################################     自动化黑盒测试报告生成完毕     ############################################################\n";
            StringBuilder sb = new StringBuilder();
            sb.append(line8);

            queue.add(sb.toString());
            if (printFlag) {
                System.out.println(line8);
            }
        }

        return queue;
    }

    /**
     * 性能报告
     * @param data
     */
    public static StringBuffer addPerformanceReport(String data){
        PERFORMANCE_COUNT.incrementAndGet();
        GenReportData peek = queueList.peek();
        peek.getPerformanceCount().incrementAndGet();
        StringBuffer performanceReport = peek.getPerformanceReport();
        performanceReport.append(data).append(" ");
        return performanceReport;
    }

    /**
     * err
     * @param data
     */
    public static StringBuffer addErrorReport(String data){
        ERROR_COUNT.incrementAndGet();
        GenReportData peek = queueList.peek();
        peek.getErrorCount().incrementAndGet();
        StringBuffer errorReport = peek.getErrorReport();
        errorReport.append(data).append(" ");
        return errorReport;
    }

    /**
     * info
     * @param data
     */
    public static StringBuffer addInfoReport(String data){
        INFO_COUNT.incrementAndGet();
        GenReportData peek = queueList.peek();
        peek.getInfoCount().incrementAndGet();
        StringBuffer infoReport = peek.getInfoReport();
        infoReport.append(data).append(" ");
        return infoReport;
    }

    /**
     * 补充报告
     * @param data
     */
    public static StringBuffer addExtraReport(String data){
        StringBuffer extraReport = queueList.peek().getExtraReport();
        extraReport.append(data).append(" ");
        return extraReport;
    }

    /***
     *
     * @param reportClassPriority
     */
    public static void newReportDataResource(Integer reportClassPriority){
        GenReportData genReportData = GenReportData.newReportData();
        genReportData.setReportClassPriority(reportClassPriority);
        priorityQueue.add(genReportData);
        queueList.add(genReportData);
    }

    @Data
    static class GenReportData {
        private Integer reportClassPriority;
        /**
         * 补充报告
         */
        private StringBuffer extraReport;
        /**
         * 错误报告
         */
        private StringBuffer errorReport;
        private AtomicInteger errorCount;
        /**
         * info报告
         */
        private StringBuffer infoReport;
        private AtomicInteger infoCount;
        /**
         * 性能报告
         */
        private StringBuffer performanceReport;
        private AtomicInteger performanceCount;

        public static GenReportData newReportData(){
            StringBuffer errorReport = new StringBuffer();
            StringBuffer performanceReport = new StringBuffer();
            StringBuffer extraReport = new StringBuffer();
            StringBuffer infoReport = new StringBuffer();
            AtomicInteger errorCount = new AtomicInteger();
            AtomicInteger infoCount = new AtomicInteger();
            AtomicInteger performanceCount = new AtomicInteger();

            GenReportData genReportData = new GenReportData();
            genReportData.setExtraReport(extraReport);
            genReportData.setErrorReport(errorReport);
            genReportData.setPerformanceReport(performanceReport);
            genReportData.setInfoReport(infoReport);
            genReportData.setErrorCount(errorCount);
            genReportData.setInfoCount(infoCount);
            genReportData.setPerformanceCount(performanceCount);
            return genReportData;
        }
    }

}
