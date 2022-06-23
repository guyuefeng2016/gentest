package com.gentest.gencode;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GenerateFileService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     *
     * @param testCaseInfoList
     * @param caseInput
     */
    public void genTestCase(List<TestCaseClassInfo> testCaseInfoList, CaseInput caseInput) {
        if (!CollectionUtils.isEmpty(testCaseInfoList)) {
            String author = null;
            String comment = null;
            String outputDir = "";
            String testCasePackage = "";
            if (caseInput != null) {
                author = caseInput.getAuthor();
                comment = caseInput.getComment();
                testCasePackage = caseInput.getTestCasePackage();
                String odr = caseInput.getOutputDir();
                outputDir = StringUtils.isNotEmpty(odr) ? odr : (System.getProperty("user.dir") + "/src/test/java/com/gentest");
            } else {
                outputDir = System.getProperty("user.dir") + "/src/test/java/com/gentest";
            }

            for (TestCaseClassInfo caseInfo : testCaseInfoList){
                genFile(testCasePackage, author, comment, caseInfo, outputDir);
            }
        }
    }


    /**
     *
     * @param testPackage 测试用例的包名
     * @param author 作者
     * @param comment 注释
     * @param caseClassInfo
     * @param outputDir 输出测试用例的位置
     */
    public void genFile(String testPackage, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir) {
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("testcase.ftl");

            GenCaseInfo caseInfo = new GenCaseInfo();
            caseInfo.setTestPackage(testPackage);
            caseInfo.setRepositoryPackage(caseClassInfo.getTestCaseRepositoryPackage());
            caseInfo.setRepositoryName(caseClassInfo.getTestCaseClassName());
            caseInfo.setAuthor(author);
            caseInfo.setComment(comment);

            List<SouceCodeInfo> methods = new LinkedList<>();
            List<TestCaseMethodInfo> testCaseMethodInfos = caseClassInfo.getTestCaseMethodInfos();
            if (!CollectionUtils.isEmpty(testCaseMethodInfos)){
                testCaseMethodInfos.forEach(caseMethodInfo -> {
                    SouceCodeInfo souceCodeInfo = new SouceCodeInfo();
                    souceCodeInfo.setReturnType(caseMethodInfo.getReturnType());
                    souceCodeInfo.setArgSize(caseMethodInfo.getMethodArgCount());
                    souceCodeInfo.setMethodName(caseMethodInfo.getTestCaseMethodName());

                    Object returnObj = caseMethodInfo.getReturnObj();
                    if (returnObj != null){
                        souceCodeInfo.setReturnObj(returnObj == "" ? null : returnObj.toString());
                    } else {
                        souceCodeInfo.setReturnObj(returnObj);
                    }
                    Map<Integer, StringBuilder> methodSourceCode = caseMethodInfo.getMethodSourceCode();
                    Map<String, String> resMethodSouceCodeMap = new LinkedHashMap<>();
                    for (Map.Entry<Integer,StringBuilder> entry: methodSourceCode.entrySet()){
                        resMethodSouceCodeMap.put(entry.getKey()+"", entry.getValue().toString());
                    }
                    souceCodeInfo.setMapSouceCode(resMethodSouceCodeMap);
                    methods.add(souceCodeInfo);
                });
            }
            caseInfo.setMethodList(methods);

            String outFile = outputDir+ File.separator+caseInfo.getRepositoryName()+"Test.java";
            log.info("-----------> 生成测试用例：{}",outFile);
            // 文件输出路径
            FileOutputStream file = new FileOutputStream(outFile);
            OutputStreamWriter out = new OutputStreamWriter(file, "utf-8");
            template.process(caseInfo, out);
            out.close();
        } catch (Exception e){
            log.error("e=",e);
        }
    }

}