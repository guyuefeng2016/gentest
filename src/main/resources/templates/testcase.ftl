<#if testPackage?? && testPackage != "">
package ${testPackage};

import ${testPackage}.BaseTest;
</#if>
<#if repositoryPackage?? && repositoryPackage != "">
import ${repositoryPackage};
</#if>
import java.time.*;
import java.util.*;
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
public class ${repositoryName}Test extends BaseTest{

    @Autowired
    private ${repositoryName?cap_first} ${repositoryName?uncap_first};

<#list methodList as methodMap>

    <#list methodMap?keys as methodName>
    @Test
    public void ${methodName}Test${methodMap_index}() throws Exception {
        <#list methodMap[methodName].mapSouceCode?keys as key>
        ${methodMap[methodName].mapSouceCode[key]}
        </#list>
        <#if methodMap[methodName].returnType == "void">
        ${repositoryName?uncap_first}.${methodName}(<#if methodMap[methodName].argSize gt 0><#list 0..methodMap[methodName].argSize-1 as i><#if i_has_next>var${i}0,<#else >var${i}0</#if></#list></#if>);
        <#else >
        ${methodMap[methodName].returnType} result${methodName?cap_first} = ${repositoryName?uncap_first}.${methodName}(<#if methodMap[methodName].argSize gt 0><#list 0..methodMap[methodName].argSize-1 as i><#if i_has_next>var${i}0,<#else >var${i}0</#if></#list></#if>);
        log.info("result${methodName?cap_first} : {}", result${methodName?cap_first});
        <#if methodMap[methodName].returnObj??>
        /**
        *  当前默认值执行之后的结果为：${methodMap[methodName].returnObj}
        */
        </#if>
        </#if>
    }
    </#list>
</#list>

}