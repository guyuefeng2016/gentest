<#if testPackage?? && testPackage != "">
package ${testPackage};

</#if>
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
* <br>
* <b>功能：</b>baseTest类<br>
* <b>作者：</b>${author!"guyuefeng"}<br>
* <b>日期：</b>${.now?string("yyyy")}<br>
*/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ${baseTestClass}.class)
public class GenBaseTest {


}
