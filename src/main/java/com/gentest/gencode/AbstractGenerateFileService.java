package com.gentest.gencode;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * @Description: 
 * @Author: guyuefeng
 * @Date: 2022/6/23
 */
@Slf4j
@Service
public abstract class AbstractGenerateFileService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;


    /**
     *
     * @param testPackage 测试用例的包名
     * @param author 作者
     * @param comment 注释
     * @param caseClassInfo
     * @param outputDir 输出测试用例的位置
     */
    public void genFile(String testPackage, String clazzName, String repositoryName, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir, String ftl) {
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate(ftl);

            Object data = genFileBody(testPackage, clazzName, author, comment, caseClassInfo, outputDir, ftl);
            String outFile = outputDir+ File.separator+repositoryName+"Test.java";
            log.info("-----------> 生成测试用例：{}",outFile);
            // 文件输出路径
            FileOutputStream file = new FileOutputStream(outFile);
            OutputStreamWriter out = new OutputStreamWriter(file, "utf-8");
            template.process(data, out);
            out.close();
        } catch (Exception e){
            log.error("e=",e);
        }
    }

    /**
     *
     * @param testPackage
     * @param clazzName
     * @param author
     * @param comment
     * @param caseClassInfo
     * @param outputDir
     * @param ftl
     * @return
     */
    public abstract Object genFileBody(String testPackage, String clazzName, String author, String comment, TestCaseClassInfo caseClassInfo, String outputDir, String ftl);
}