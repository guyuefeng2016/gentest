package com.gentest.gencode;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: guyuefeng
 * @Date: 2022/6/23
 */
@Slf4j
@Service
public class GenerateFileService extends AbstractGenerateFileService{

    @Autowired
    private GenerateBaseCaseService generateBaseCaseService;

    /**
     *
     * @param testCaseInfoList
     * @param caseInput
     * @param applicationClass
     */
    public void genTestCase(List<TestCaseClassInfo> testCaseInfoList, CaseInput caseInput, Class applicationClass) {
        if (!CollectionUtils.isEmpty(testCaseInfoList)) {
            String author = null;
            String comment = null;
            String outputDir = "";
            String testCasePackage = "";
            boolean autoDir = false;
            if (caseInput != null) {
                author = caseInput.getAuthor();
                comment = caseInput.getComment();
                testCasePackage = caseInput.getTestCasePackage();
                String odr = caseInput.getOutputDir();
                if (StringUtils.isNotEmpty(odr) ){
                    outputDir = odr;
                } else {
                    autoDir = true;
                }
            } else {
                autoDir = true;
            }
            if (autoDir) {
                outputDir = System.getProperty("user.dir") + "/src/test/com/gentest";
                testCasePackage = "com.gentest";

                File file = new File(outputDir);
                if (!file.exists()) {
                    boolean mkdirs = file.mkdirs();
                    if (!mkdirs) {
                        outputDir = System.getProperty("user.dir") + "/src/test/";
                        testCasePackage = "";
                    }
                }
            }

            generateBaseCaseService.genTestCase(testCasePackage, author, comment, null, outputDir, applicationClass.getName());
            for (TestCaseClassInfo caseInfo : testCaseInfoList){
                genFile(testCasePackage, "", caseInfo.getTestCaseClassName(), author, comment, caseInfo, outputDir, "testcase.ftl");
            }
        }
    }


    @Override
    public Object genFileBody(String testPackage, String clazzName, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir, String ftl) {
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
        return caseInfo;
    }


}