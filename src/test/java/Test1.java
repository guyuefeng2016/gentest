//import com.gentest.gencode.GenCaseInfo;
//import com.gentest.gencode.SouceCodeInfo;
//import freemarker.template.Configuration;
//import freemarker.template.Template;
//import freemarker.template.TemplateException;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
//
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.util.LinkedHashMap;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
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
//        List<SouceCodeInfo> methods = new LinkedList<>();
//
//        SouceCodeInfo souceCodeInfo = new SouceCodeInfo();
//        souceCodeInfo.setArgSize(1);
//        souceCodeInfo.setMethodName("saveUser");
//        Map<String, String> mapSouceCode = new LinkedHashMap<>();
//        mapSouceCode.put("1","List var1 = new LinkedList<>();");
//        souceCodeInfo.setMapSouceCode(mapSouceCode);
//
//
//        SouceCodeInfo souceCodeInfo2 = new SouceCodeInfo();
//        souceCodeInfo2.setArgSize(1);
//        souceCodeInfo2.setMethodName("saveUser");
//        souceCodeInfo.setReturnObj("3");
//        souceCodeInfo.setReturnType("String");
//        Map<String, String> mapSouceCode2 = new LinkedHashMap<>();
//        mapSouceCode2.put("1","Map var1 = new HashMap<>();");
//        souceCodeInfo2.setMapSouceCode(mapSouceCode2);
//
//        SouceCodeInfo souceCodeInfo3 = new SouceCodeInfo();
//        souceCodeInfo3.setArgSize(0);
//        souceCodeInfo3.setMethodName("saveUser");
//        Map<String, String> mapSouceCode3 = new LinkedHashMap<>();
//        mapSouceCode3.put("0","String var1 = new String<>();");
//        souceCodeInfo3.setMapSouceCode(mapSouceCode3);
//
//        methods.add(souceCodeInfo);
//        methods.add(souceCodeInfo2);
//        methods.add(souceCodeInfo3);
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
