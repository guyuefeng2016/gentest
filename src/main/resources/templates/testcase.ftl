<#if testPackage?? && testPackage != "">
package ${testPackage};

</#if>
<#if repositoryPackage?? && repositoryPackage != "">
import ${repositoryPackage};
</#if>
import java.time.*;
import java.util.*;
import java.math.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
/**
* <br>
* <b>功能：</b>${comment!repositoryName} 测试类<br>
* <b>作者：</b>${author!"guyuefeng"}<br>
* <b>日期：</b>${.now?string("yyyy")}<br>
*/
@Slf4j
public class ${repositoryName}Test extends GenBaseTest{

    @Autowired
    private ${repositoryName?cap_first} ${repositoryName?uncap_first};

<#list methodList as souceCode>

 <#if souceCode.methodName??>
    @Test
    public void ${souceCode.methodName}Test${souceCode_index}() throws Exception {
        <#list souceCode.mapSouceCode?keys as key>
        ${souceCode.mapSouceCode[key]}
        </#list>
        <#if souceCode.returnType! == "void">
        ${repositoryName?uncap_first}.${souceCode.methodName}(<#if souceCode.argSize gt 0><#list 0..souceCode.argSize-1 as i><#if i_has_next>var${i}0,<#else >var${i}0</#if></#list></#if>);
        <#else >
        ${souceCode.returnType!} result${souceCode.methodName?cap_first} = ${repositoryName?uncap_first}.${souceCode.methodName}(<#if souceCode.argSize gt 0><#list 0..souceCode.argSize-1 as i><#if i_has_next>var${i}0,<#else >var${i}0</#if></#list></#if>);
        log.info("result${souceCode.methodName?cap_first} : {}", result${souceCode.methodName?cap_first});
        <#if souceCode.returnType?contains("Boolean") >
        assert !result${souceCode.methodName?cap_first};
        <#elseif souceCode.returnType?contains("IPage")>
        assert result${souceCode.methodName?cap_first}.getRecords().size() > 0;
        <#elseif souceCode.returnType?contains("PageVo")>
        assert result${souceCode.methodName?cap_first}.getCount() > 0;
        <#elseif souceCode.returnType?contains("PageDto")>
        assert result${souceCode.methodName?cap_first}.getCount() > 0;
        <#elseif souceCode.returnType?starts_with("java.util.List")>
        assert result${souceCode.methodName?cap_first}.size() > 0;
        <#elseif souceCode.returnType?starts_with("java.util.Map")>
        assert result${souceCode.methodName?cap_first}.size() > 0;
        <#else >
        assert result${souceCode.methodName?cap_first} != null;
        </#if>
        <#if souceCode.returnObj??>
        /**
        *  当前给定参数执行结果为：${souceCode.returnObj}
        */
        </#if>
        </#if>
    }
</#if>
</#list>

}