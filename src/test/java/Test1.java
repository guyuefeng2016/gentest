//import com.alibaba.fastjson.JSONObject;
//import com.gentest.gencode.GenCaseInfo;
//import com.gentest.gencode.SouceCodeInfo;
//import freemarker.template.Configuration;
//import freemarker.template.Template;
//import freemarker.template.TemplateException;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.util.CollectionUtils;
//import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.*;
//
///**
// * @description:
// * @author: guyuefeng
// * @create: 2022-06-20 19:50
// **/
//@Slf4j
//public class Test1 extends BaseTest {
//
//    @Autowired
//    private FreeMarkerConfigurer freeMarkerConfigurer;
//
//    @Test
//    public void test1() throws IOException {
//        Configuration configuration = freeMarkerConfigurer.getConfiguration();
//        System.out.println(configuration);
//
//        Template template = configuration.getTemplate("my.ftl");
//        System.out.println(template.getName());
//    }
//
//    @Test
//    public void test2() throws IOException, TemplateException {
//        Configuration configuration = freeMarkerConfigurer.getConfiguration();
//        System.out.println(configuration);
//
//        Template template = configuration.getTemplate("testcase.ftl");
//
//        GenCaseInfo caseInfo = new GenCaseInfo();
//        caseInfo.setTestPackage("");
//        caseInfo.setRepositoryPackage("");
//        caseInfo.setRepositoryName("UserInfoRepository");
//        caseInfo.setAuthor("guyuefeng");
//        caseInfo.setComment("comment");
//
//        List<Map<String, SouceCodeInfo>> methods = new LinkedList<>();
//
//        Map<String, SouceCodeInfo> map1 = new LinkedHashMap<>();
//        SouceCodeInfo souceCodeInfo = new SouceCodeInfo();
//        souceCodeInfo.setArgSize(1);
//        Map<String, String> mapSouceCode = new LinkedHashMap<>();
//        mapSouceCode.put("1","List var1 = new LinkedList<>();");
//        souceCodeInfo.setMapSouceCode(mapSouceCode);
//        map1.put("saveUser", souceCodeInfo);
//
//        Map<String, SouceCodeInfo> map2 = new LinkedHashMap<>();
//        SouceCodeInfo souceCodeInfo2 = new SouceCodeInfo();
//        souceCodeInfo2.setArgSize(1);
//        Map<String, String> mapSouceCode2 = new LinkedHashMap<>();
//        mapSouceCode2.put("1","Map var1 = new HashMap<>();");
//        souceCodeInfo2.setMapSouceCode(mapSouceCode2);
//        map2.put("saveUser", souceCodeInfo2);
//
//        Map<String, SouceCodeInfo> map3 = new LinkedHashMap<>();
//        SouceCodeInfo souceCodeInfo3 = new SouceCodeInfo();
//        souceCodeInfo3.setArgSize(0);
//        Map<String, String> mapSouceCode3 = new LinkedHashMap<>();
//        mapSouceCode3.put("0","String var1 = new String<>();");
//        souceCodeInfo3.setMapSouceCode(mapSouceCode3);
//        map3.put("saveUser", souceCodeInfo3);
//
//        methods.add(map1);
//        methods.add(map2);
//        methods.add(map3);
//
//
//        caseInfo.setMethodList(methods);
//
//        // 文件输出路径
//        FileOutputStream file = new FileOutputStream("/Users/huangyechuang/Downloads/gentest/src/test/java/"+caseInfo.getRepositoryName()+"Test.java");
//        OutputStreamWriter out = new OutputStreamWriter(file, "utf-8");
//        template.process(caseInfo, out);
//        out.close();
//    }
//}
