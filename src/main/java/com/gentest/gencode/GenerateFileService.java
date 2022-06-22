package com.gentest.gencode;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

@Slf4j
@Service
public class GenerateFileService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     *
     * @param testPackage 测试用例的包名
     * @param repositoryPackage 测试包名+测试类名
     * @param className 测试类名
     * @param author 作者
     * @param comment 注释
     * @param methodsMapList 非字符串的空，传"null"，字符串的"" 传 \"\"
     * @param outputDir 输出测试用例的位置
     * @throws IOException
     * @throws TemplateException
     */
    public void genTestCase(String testPackage, String repositoryPackage, String className, String author, String comment, List<Map<String, Object[]>> methodsMapList, List<Map<Integer, StringBuilder>> methodSouceCodeMap, List<Map<String, Object>> returnTypeList, String outputDir) {
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            Template template = configuration.getTemplate("testcase.ftl");

            GenCaseInfo caseInfo = new GenCaseInfo();
            caseInfo.setTestPackage(testPackage);
            caseInfo.setRepositoryPackage(repositoryPackage);
            caseInfo.setRepositoryName(className);
            caseInfo.setAuthor(author);
            caseInfo.setComment(comment);

            List<Map<String,SouceCodeInfo>> methods = new LinkedList<>();

            if (!CollectionUtils.isEmpty(methodsMapList)){
                for (int index=0,methodLen=methodsMapList.size(); index<methodLen; index++){
                    Map<String, SouceCodeInfo> resMap = new LinkedHashMap<>();

                    Map<String, Object[]> map = methodsMapList.get(index);
                    Map<Integer, StringBuilder> methodSouceCode = methodSouceCodeMap.get(index);

                    Map<String, Object> returnTypeMap = returnTypeList.get(index);
                    String returnTypeKey = returnTypeMap.keySet().iterator().next();
                    Object returnObj = returnTypeMap.get(returnTypeKey);

                    Map<String, String> resMethodSouceCodeMap = new LinkedHashMap<>();
                    for (Map.Entry<Integer,StringBuilder> entry: methodSouceCode.entrySet()){
                        resMethodSouceCodeMap.put(entry.getKey()+"", entry.getValue().toString());
                    }

                    for (Map.Entry<String,Object[]> entry: map.entrySet()){
                        Object[] values = entry.getValue();

                        SouceCodeInfo souceCodeInfo = new SouceCodeInfo();
                        souceCodeInfo.setArgSize(values.length);
                        souceCodeInfo.setMapSouceCode(resMethodSouceCodeMap);
                        if (returnObj != null){
                            souceCodeInfo.setReturnObj(returnObj == "" ? null : returnObj.toString());
                        } else {
                            souceCodeInfo.setReturnObj(returnObj);
                        }

                        souceCodeInfo.setReturnType(returnTypeKey);

                        resMap.put(entry.getKey(), souceCodeInfo);
                        methods.add(resMap);
                    }
                }
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