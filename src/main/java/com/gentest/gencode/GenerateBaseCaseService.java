package com.gentest.gencode;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author: guyuefeng
 * @Date: 2022/6/23
 */
@Slf4j
@Service
public class GenerateBaseCaseService extends AbstractGenerateFileService{


    /**
     *
     * @param testPackage
     * @param author
     * @param comment
     * @param caseClassInfo
     * @param outputDir
     * @param baseTestClass
     */
    public void genTestCase(String testPackage, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir, String baseTestClass) {
        genFile(testPackage, baseTestClass,"GenBase", author, comment, null, outputDir, "baseTest.ftl");
    }

    @Override
    public Object genFileBody(String testPackage, String clazzName, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir, String ftl) {
        GenBaseCaseInfo caseInfo = new GenBaseCaseInfo();
        caseInfo.setTestPackage(testPackage);
        caseInfo.setAuthor(author);
        caseInfo.setComment(comment);
        caseInfo.setBaseTestClass(clazzName);

        return caseInfo;
    }

}